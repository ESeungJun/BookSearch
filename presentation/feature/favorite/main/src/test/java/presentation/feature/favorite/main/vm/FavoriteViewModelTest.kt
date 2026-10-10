package presentation.feature.favorite.main.vm

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import domain.favorite.usecase.ObserveFavoritesUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import presentation.base.R as BaseR
import presentation.feature.favorite.main.data.FavoriteUiStatus
import presentation.feature.favorite.main.data.PriceRange
import presentation.feature.favorite.main.data.TitleSort

/** 화면이 정하는 판단만 확인한다: 조건 전달·제목 정렬·개수·빈 상태 구분·토글·오류. 거르는 쿼리는 DAO 테스트가 맡는다. */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeFavoriteRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `즐겨찾기가 없으면 저장 없음 상태다`() = runTest {
        val viewModel = started()
        assertEquals(FavoriteUiStatus.NoFavorites, viewModel.uiState.value.status)
    }

    @Test
    fun `기본은 제목 오름차순이고 제목이 없는 책은 어느 방향이든 맨 뒤다`() = runTest {
        repository.books.value = listOf(book("1", "b"), book("2", null), book("3", "A"), book("4", "c"))
        val viewModel = started()
        assertEquals(listOf("3", "1", "4", "2"), viewModel.uiState.value.books.map { it.key })

        viewModel.onSortChange(TitleSort.DESCENDING)
        assertEquals(listOf("4", "1", "3", "2"), viewModel.uiState.value.books.map { it.key })
    }

    @Test
    fun `검색어는 앞뒤 공백을 빼고, 금액은 구간 범위로 넘긴다`() = runTest {
        repository.books.value = listOf(book("1", "코틀린"))
        val viewModel = started()
        viewModel.onQueryChange("  코틀린 ")
        viewModel.onPriceRangeChange(PriceRange.FROM_10K_TO_20K)
        advanceUntilIdle()
        assertEquals("코틀린" to 10_000..19_999, repository.calls.last())
    }

    @Test
    fun `개수 줄은 거른 수와 전체 수다`() = runTest {
        repository.books.value = listOf(book("1", "코틀린 인 액션"), book("2", "자바"), book("3", "코틀린 코루틴"))
        val viewModel = started()
        viewModel.onQueryChange("코틀린")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.books.size)
        assertEquals(3, state.totalCount)
        assertEquals(FavoriteUiStatus.Results, state.status)
    }

    @Test
    fun `조건에 맞는 책이 없으면 조건 불일치 상태이고 초기화하면 다시 모두 보인다`() = runTest {
        repository.books.value = listOf(book("1", "코틀린"))
        val viewModel = started()
        viewModel.onQueryChange("자바")
        viewModel.onPriceRangeChange(PriceRange.OVER_30K)
        advanceUntilIdle()
        assertEquals(FavoriteUiStatus.NoMatch, viewModel.uiState.value.status)

        viewModel.onResetFilters()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("" to PriceRange.ALL, state.query to state.priceRange)
        assertEquals(FavoriteUiStatus.Results, state.status)
        assertEquals("" to null, repository.calls.last())
    }

    @Test
    fun `하트를 누르면 즐겨찾기에서 빼고 목록에서 사라진다`() = runTest {
        repository.books.value = listOf(book("1", "a"), book("2", "b"))
        val viewModel = started()
        viewModel.onFavoriteClick("1")
        advanceUntilIdle()

        assertEquals("1" to true, repository.toggled.single())
        assertEquals(listOf("2"), viewModel.uiState.value.books.map { it.key })
        assertEquals(1, viewModel.uiState.value.totalCount)
    }

    @Test
    fun `DB 를 읽지 못하면 오류 상태이고 다시 시도하면 다시 관찰한다`() = runTest {
        repository.failure = DomainResult.Error(IllegalStateException())
        val viewModel = started()
        assertEquals(FavoriteUiStatus.Error(BaseR.string.result_retry_later), viewModel.uiState.value.status)

        repository.failure = null
        repository.books.value = listOf(book("1", "a"))
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(FavoriteUiStatus.Results, viewModel.uiState.value.status)
    }

    private fun TestScope.started(): FavoriteViewModel {
        val viewModel = FavoriteViewModel(
            observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(repository),
        )
        advanceUntilIdle()
        return viewModel
    }

    private fun book(key: String, title: String?) = BookDTO(
        key = key, title = title, authors = null, publisher = null, publishedDate = null,
        price = null, salePrice = null, thumbnailUrl = null, isbn = null, description = null, url = null,
    )

    /** 검색어(제목 포함)만 흉내 낸다. 금액은 넘어온 값만 기록한다. */
    private class FakeFavoriteRepository : IFavoriteRepository {
        val books = MutableStateFlow<List<BookDTO>>(emptyList())
        val calls = mutableListOf<Pair<String, IntRange?>>()
        val toggled = mutableListOf<Pair<String, Boolean>>()
        var failure: DomainResult<Nothing>? = null

        override fun observeFavorites(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> {
            calls += query to priceRange
            failure?.let { return flowOf(it) }
            // 금액 구간이 있으면 가격이 없는 테스트 책은 모두 빠진다(실제 쿼리와 같다)
            return books.map { list ->
                DomainResult.Success(list.filter { query in it.title.orEmpty() && priceRange == null })
            }
        }

        override fun observeFavoriteKeys(): Flow<DomainResult<Set<String>>> = throw UnsupportedOperationException()

        override suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean): DomainResult<Unit> {
            toggled += book.key to isFavorite
            books.value = books.value.filterNot { it.key == book.key }
            return DomainResult.Success(Unit)
        }
    }
}
