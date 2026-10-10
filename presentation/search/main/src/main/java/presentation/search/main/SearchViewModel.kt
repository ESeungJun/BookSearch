package presentation.search.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.usecase.ObserveFavoriteKeysUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.usecase.LoadMoreBooksUseCase
import domain.search.usecase.SearchBooksUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
import presentation.base.BookViewData
import presentation.base.R
import presentation.base.failureMessageRes
import presentation.base.isServiceConfigError
import presentation.base.toViewData

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val loadMoreBooksUseCase: LoadMoreBooksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeFavoriteKeysUseCase: ObserveFavoriteKeysUseCase,
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
                // 키를 못 읽으면 하트만 이전 상태로 남는다. 목록은 그대로 쓸 수 있어 화면 상태를 바꾸지 않는다
                if (result is DomainResult.Success) {
                    favoriteKeys = result.data
                    _uiState.update { it.copy(books = bookViewData()) }
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
        if (state.status != SearchStatus.Results) return
        if (state.loadMore != LoadMoreState.READY && state.loadMore != LoadMoreState.FAILED) return
        if (requestJob?.isActive == true) return // 새로고침 중
        val nextPage = state.page + 1
        _uiState.update { it.copy(loadMore = LoadMoreState.LOADING) }
        requestJob = viewModelScope.launch {
            onNextPage(loadMoreBooksUseCase(state.searchedQuery, state.sort, nextPage), nextPage)
        }
    }

    private fun onNextPage(result: DomainResult<SearchPageDTO>, page: Int) {
        if (result !is DomainResult.Success) {
            // 다음 페이지 실패는 화면 단위가 아니다. 받은 목록을 두고 목록 끝에서 다시 시도한다
            _uiState.update { it.copy(loadMore = LoadMoreState.FAILED) }
            return
        }
        val data = result.data
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
                toastRes = toastFor(data) ?: it.toastRes,
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

    fun onToastShown() {
        _uiState.update { it.copy(toastRes = null) }
    }

    /** 새 검색·정렬 변경·새로고침·다시 시도 모두 첫 페이지부터 다시 받는다. */
    private fun search(query: String, isRefresh: Boolean = false) {
        requestJob?.cancel()
        if (query.isEmpty()) {
            loadedBooks = emptyList()
            _uiState.update {
                it.copy(status = SearchStatus.Idle, books = persistentListOf(), notice = null, isRefreshing = false)
            }
            return
        }
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true) else it.copy(status = SearchStatus.Loading, notice = null)
        }
        val sort = _uiState.value.sort
        requestJob = viewModelScope.launch { onFirstPage(query, searchBooksUseCase(query, sort)) }
    }

    private fun onFirstPage(query: String, result: DomainResult<SearchPageDTO>) {
        if (result !is DomainResult.Success) {
            loadedBooks = emptyList()
            _uiState.update {
                it.copy(
                    status = SearchStatus.Error(result.failureMessageRes()),
                    searchedQuery = query,
                    books = persistentListOf(),
                    notice = null,
                    isRefreshing = false,
                )
            }
            return
        }
        val data = result.data
        loadedBooks = data.books.distinctBy { it.key }
        _uiState.update {
            it.copy(
                status = if (loadedBooks.isEmpty()) SearchStatus.Empty else SearchStatus.Results,
                searchedQuery = query,
                books = bookViewData(),
                totalCount = data.totalCount,
                page = FIRST_PAGE,
                loadMore = if (data.isEnd) LoadMoreState.END else LoadMoreState.READY,
                isRefreshing = false,
                notice = noticeFor(data),
                toastRes = toastFor(data) ?: it.toastRes,
            )
        }
    }

    private fun bookViewData(): ImmutableList<BookViewData> =
        loadedBooks.map { it.toViewData(isFavorite = it.key in favoriteKeys) }.toImmutableList()

    // 네트워크가 실패해 저장된 결과·저장된 책에서 찾은 결과를 보여 줄 때 그 사실을 안내 줄로 알린다
    private fun noticeFor(page: SearchPageDTO): SearchNotice? {
        val cachedAt = page.cachedAt
        return when {
            cachedAt != null -> SearchNotice.Cached(formatTime(cachedAt))
            page.isLocalMatch -> SearchNotice.LocalMatch
            else -> null
        }
    }

    // 키 오류인데 저장된 결과로 덮여 원인이 안 보이는 경우만 토스트로 알린다
    private fun toastFor(page: SearchPageDTO): Int? =
        if (page.failure?.isServiceConfigError() == true) {
            R.string.result_service_config
        } else {
            null
        }

    private fun formatTime(epochMillis: Long): String =
        DateTimeFormatter.ofPattern(CACHE_TIME_PATTERN)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(epochMillis))

    companion object {
        const val QUERY_DEBOUNCE_MS = 400L
        private const val FIRST_PAGE = 1
        private const val CACHE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
    }
}
