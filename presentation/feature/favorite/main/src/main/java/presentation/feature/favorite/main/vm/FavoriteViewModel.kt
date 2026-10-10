package presentation.feature.favorite.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.usecase.ObserveFavoritesUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.base.data.BookViewData
import presentation.base.data.failureMessageRes
import presentation.base.mapper.toViewData
import presentation.feature.favorite.main.data.FavoriteUiStatus
import presentation.feature.favorite.main.data.FavoriteUiState
import presentation.feature.favorite.main.data.PriceRange
import presentation.feature.favorite.main.data.TitleSort

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    // 조건에 맞는 책(최근에 넣은 순). 하트를 누르면 이 책을 그대로 넘긴다
    private var filteredBooks: List<BookDTO> = emptyList()

    // DB 읽기가 실패하면 관찰이 끝난다. 다시 시도는 이 관찰을 새로 시작한다
    private var observeJob: Job? = null

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    // 로컬 조회라 입력마다 바로 거른다(디바운스 없음)
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onPriceRangeChange(priceRange: PriceRange) {
        _uiState.update { it.copy(priceRange = priceRange) }
    }

    fun onSortChange(sort: TitleSort) {
        _uiState.update { it.copy(sort = sort, books = sortedViewData(sort)) }
    }

    fun onResetFilters() {
        _uiState.update { it.copy(query = "", priceRange = PriceRange.ALL) }
    }

    fun onFavoriteClick(key: String) {
        val book = filteredBooks.find { it.key == key } ?: return
        // 이 목록의 책은 모두 즐겨찾기다. 빼면 관찰 결과에서 사라진다
        viewModelScope.launch { toggleFavoriteUseCase(book, isFavorite = true) }
    }

    fun retry() {
        _uiState.update { it.copy(status = FavoriteUiStatus.Loading) }
        observe()
    }

    /** 조건에 맞는 책과 조건 없는 전체를 함께 관찰한다. 전체 수는 "m / n권" 과 빈 상태 구분(저장 없음·조건 불일치)에 쓴다. */
    private fun observe() {
        observeJob?.cancel()
        // 앞뒤 공백만 다른 입력은 같은 조건이다. 조건이 바뀌면 이전 조건의 관찰을 멈춘다
        val filtered = _uiState
            .map { it.query.trim() to it.priceRange }
            .distinctUntilChanged()
            .flatMapLatest { (query, priceRange) -> observeFavoritesUseCase(query, priceRange.range) }
        val all = observeFavoritesUseCase(query = "", priceRange = null)
        observeJob = combine(filtered, all, ::onFavorites).launchIn(viewModelScope)
    }

    private fun onFavorites(filtered: DomainResult<List<BookDTO>>, all: DomainResult<List<BookDTO>>) {
        if (filtered !is DomainResult.Success) return onFailure(filtered)
        if (all !is DomainResult.Success) return onFailure(all)
        filteredBooks = filtered.data
        _uiState.update {
            it.copy(
                status = when {
                    all.data.isEmpty() -> FavoriteUiStatus.NoFavorites
                    filtered.data.isEmpty() -> FavoriteUiStatus.NoMatch
                    else -> FavoriteUiStatus.Results
                },
                books = sortedViewData(it.sort),
                totalCount = all.data.size,
            )
        }
    }

    private fun onFailure(result: DomainResult<*>) {
        filteredBooks = emptyList()
        _uiState.update { it.copy(status = FavoriteUiStatus.Error(result.failureMessageRes()), books = persistentListOf()) }
    }

    // 정렬은 화면이 정한다. 같은 제목은 최근에 넣은 순서를 지킨다(sortedWith 는 안정 정렬)
    private fun sortedViewData(sort: TitleSort): ImmutableList<BookViewData> {
        val titleOrder = when (sort) {
            TitleSort.ASCENDING -> String.CASE_INSENSITIVE_ORDER
            TitleSort.DESCENDING -> String.CASE_INSENSITIVE_ORDER.reversed()
        }
        return filteredBooks
            .map { it.toViewData(isFavorite = true) }
            .sortedWith(compareBy(nullsLast(titleOrder)) { it.title })
            .toImmutableList()
    }
}
