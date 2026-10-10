package presentation.feature.search.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.usecase.ObserveFavoriteKeysUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.usecase.SearchBooksUseCase
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.base.data.BookViewData
import presentation.base.mapper.toViewData
import presentation.feature.search.main.data.LoadMoreState
import presentation.feature.search.main.data.SearchNotice
import presentation.feature.search.main.data.SearchUiStatus
import presentation.feature.search.main.data.SearchUiState
import presentation.feature.search.main.mapper.toCacheTimeText

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    observeFavoriteKeysUseCase: ObserveFavoriteKeysUseCase,
) : ViewModel() {

    // 카드(BookViewData)는 받은 책과 즐겨찾기 키를 합쳐 만든다. 검색 결과에 즐겨찾기 여부를 저장하지 않으려고 둘을 따로 둔다
    private var loadedBooks: List<BookDTO> = emptyList()
    private var favoriteKeys: Set<String> = emptySet()

    // 첫 페이지·다음 페이지 요청은 한 번에 하나다. 새 요청이 이전 요청을 취소해 늦게 온 옛 응답이 새 결과를 덮지 않는다
    private var requestJob: Job? = null

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // 입력이 멈춘 뒤에만 검색한다. 앞뒤 공백만 다른 입력은 같은 검색이다
        _uiState.map { it.query.trim() }
            .distinctUntilChanged()
            .debounce(QUERY_DEBOUNCE_MS)
            .onEach { search(it) }
            .launchIn(viewModelScope)

        observeFavoriteKeysUseCase()
            .onEach { result ->
                when (result) {
                    is DomainResult.Success -> {
                        favoriteKeys = result.data
                        _uiState.update { it.copy(books = bookViewData()) }
                    }
                    // 키를 못 읽으면 하트만 이전 상태로 남는다. 목록은 그대로 쓸 수 있어 화면 상태를 바꾸지 않는다
                    is DomainResult.Fail, is DomainResult.Error -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        // 비우면 디바운스를 기다리지 않고 바로 첫 진입 상태로 돌아간다
        if (query.isBlank()) search("")
    }

    fun onSortChange(sort: SearchSort) {
        if (sort == _uiState.value.sort) return
        _uiState.update { it.copy(sort = sort) }
        search(_uiState.value.query.trim())
    }

    fun refresh() {
        search(_uiState.value.query.trim(), isRefresh = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.status != SearchUiStatus.Results) return
        if (state.loadMore != LoadMoreState.READY && state.loadMore != LoadMoreState.FAILED) return
        if (requestJob?.isActive == true) return // 새로고침 중
        val nextPage = state.page + 1
        _uiState.update { it.copy(loadMore = LoadMoreState.LOADING) }
        requestJob = viewModelScope.launch {
            onNextPage(searchBooksUseCase(state.searchedQuery, state.sort, nextPage), nextPage)
        }
    }

    private fun onNextPage(result: DomainResult<SearchPageDTO>, page: Int) {
        when (result) {
            is DomainResult.Success -> appendPage(result.data, page)
            // 다음 페이지 실패는 화면 단위가 아니다. 받은 목록을 두고 목록 끝에서 다시 시도한다
            is DomainResult.Fail, is DomainResult.Error -> _uiState.update { it.copy(loadMore = LoadMoreState.FAILED) }
        }
    }

    private fun appendPage(data: SearchPageDTO, page: Int) {
        val knownKeys = loadedBooks.mapTo(HashSet()) { it.key }
        // 마지막 페이지를 넘기면 서버가 같은 페이지를 다시 보내므로 이미 받은 책은 뺀다(목록 key 가 겹치면 안 된다)
        val newBooks = data.books.filter { knownKeys.add(it.key) }
        loadedBooks = loadedBooks + newBooks
        _uiState.update {
            it.copy(
                books = bookViewData(),
                page = page,
                // 새 책이 하나도 없으면 더 받아도 같은 결과라 끝으로 본다
                loadMore = if (data.isEnd || newBooks.isEmpty()) LoadMoreState.END else LoadMoreState.READY,
                notice = noticeFor(data) ?: it.notice,
            )
        }
    }

    fun retry() {
        search(_uiState.value.query.trim())
    }

    fun onFavoriteClick(key: String) {
        val book = loadedBooks.find { it.key == key } ?: return
        // 결과는 즐겨찾기 키 관찰로 하트에 반영된다
        viewModelScope.launch { toggleFavoriteUseCase(book, isFavorite = key in favoriteKeys) }
    }

    /** 새 검색·정렬 변경·새로고침·다시 시도 모두 첫 페이지부터 다시 받는다. */
    private fun search(query: String, isRefresh: Boolean = false) {
        requestJob?.cancel()
        if (query.isEmpty()) {
            loadedBooks = emptyList()
            _uiState.update {
                it.copy(status = SearchUiStatus.Idle, books = persistentListOf(), notice = null, isRefreshing = false)
            }
            return
        }
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true) else it.copy(status = SearchUiStatus.Loading, notice = null)
        }
        val sort = _uiState.value.sort
        requestJob = viewModelScope.launch { onFirstPage(query, searchBooksUseCase(query, sort, FIRST_PAGE)) }
    }

    private fun onFirstPage(query: String, result: DomainResult<SearchPageDTO>) {
        when (result) {
            is DomainResult.Success -> showFirstPage(query, result.data)
            is DomainResult.Fail, is DomainResult.Error -> showError(query)
        }
    }

    private fun showError(query: String) {
        loadedBooks = emptyList()
        _uiState.update {
            it.copy(
                status = SearchUiStatus.Error,
                searchedQuery = query,
                books = persistentListOf(),
                notice = null,
                isRefreshing = false,
            )
        }
    }

    private fun showFirstPage(query: String, data: SearchPageDTO) {
        loadedBooks = data.books.distinctBy { it.key }
        _uiState.update {
            it.copy(
                status = if (loadedBooks.isEmpty()) SearchUiStatus.Empty else SearchUiStatus.Results,
                searchedQuery = query,
                books = bookViewData(),
                totalCount = data.totalCount,
                page = FIRST_PAGE,
                loadMore = if (data.isEnd) LoadMoreState.END else LoadMoreState.READY,
                isRefreshing = false,
                notice = noticeFor(data),
            )
        }
    }

    private fun bookViewData(): ImmutableList<BookViewData> =
        loadedBooks.map { it.toViewData(isFavorite = it.key in favoriteKeys) }.toImmutableList()

    // 네트워크가 실패해 저장된 결과·저장된 책에서 찾은 결과를 보여 줄 때 그 사실을 안내 줄로 알린다
    private fun noticeFor(page: SearchPageDTO): SearchNotice? {
        val cachedAt = page.cachedAt
        return when {
            cachedAt != null -> SearchNotice.Cached(cachedAt.toCacheTimeText())
            page.isLocalMatch -> SearchNotice.LocalMatch
            else -> null
        }
    }

    companion object {
        const val QUERY_DEBOUNCE_MS = 400L
        private const val FIRST_PAGE = 1
    }
}
