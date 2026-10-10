package presentation.feature.detail.main.vm

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.detail.repo.IDetailRepository
import domain.detail.usecase.GetBookUseCase
import domain.favorite.repo.IFavoriteRepository
import domain.favorite.usecase.ObserveFavoriteKeysUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import presentation.base.R as BaseR
import presentation.feature.detail.main.data.DetailUiStatus

/** 상세의 상태 구분(찾음·없음·오류)과 하트를 확인한다. 표시 값의 경계는 매퍼 테스트가 맡는다. */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val detail = FakeDetailRepository()
    private val favorite = FakeFavoriteRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `저장된 책을 찾으면 정상 상태다`() = runTest {
        detail.result = DomainResult.Success(book("k"))
        val status = started("k").uiState.value.status
        assertEquals("k", (status as DetailUiStatus.Loaded).book.title)
    }

    @Test
    fun `키에 해당하는 책이 없으면 책 없음 상태다`() = runTest {
        detail.result = DomainResult.Success(null)
        assertEquals(DetailUiStatus.NotFound, started("k").uiState.value.status)
    }

    @Test
    fun `DB 를 읽지 못하면 오류 상태이고 다시 시도하면 다시 찾는다`() = runTest {
        detail.result = DomainResult.Error(IllegalStateException())
        val viewModel = started("k")
        assertEquals(DetailUiStatus.Error(BaseR.string.result_retry_later), viewModel.uiState.value.status)

        detail.result = DomainResult.Success(book("k"))
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.status is DetailUiStatus.Loaded)
        assertEquals(listOf("k", "k"), detail.calls)
    }

    @Test
    fun `하트는 즐겨찾기 키에서 계산하고 누르면 지금 상태와 함께 토글한다`() = runTest {
        detail.result = DomainResult.Success(book("k"))
        favorite.keys.value = setOf("k")
        val viewModel = started("k")
        assertTrue(viewModel.uiState.value.isFavorite)

        viewModel.onFavoriteClick()
        advanceUntilIdle()
        assertEquals("k" to true, favorite.toggled.single())
        assertFalse(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `책을 찾지 못했으면 하트를 눌러도 아무 일도 없다`() = runTest {
        detail.result = DomainResult.Success(null)
        val viewModel = started("k")
        viewModel.onFavoriteClick()
        advanceUntilIdle()
        assertTrue(favorite.toggled.isEmpty())
    }

    private fun TestScope.started(bookId: String): DetailViewModel {
        val viewModel = DetailViewModel(
            bookId = bookId,
            getBookUseCase = GetBookUseCase(detail),
            observeFavoriteKeysUseCase = ObserveFavoriteKeysUseCase(favorite),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favorite),
        )
        advanceUntilIdle()
        return viewModel
    }

    private fun book(key: String) = BookDTO(
        key = key, title = key, authors = null, publisher = null, publishedDate = null,
        price = null, salePrice = null, thumbnailUrl = null, isbn = null, description = null, url = null,
    )

    private class FakeDetailRepository : IDetailRepository {
        var result: DomainResult<BookDTO?> = DomainResult.Success(null)
        val calls = mutableListOf<String>()

        override suspend fun getBook(key: String): DomainResult<BookDTO?> {
            calls += key
            return result
        }
    }

    private class FakeFavoriteRepository : IFavoriteRepository {
        val keys = MutableStateFlow<Set<String>>(emptySet())
        val toggled = mutableListOf<Pair<String, Boolean>>()

        override fun observeFavorites(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> =
            throw UnsupportedOperationException()

        override fun observeFavoriteKeys(): Flow<DomainResult<Set<String>>> = keys.map { DomainResult.Success(it) }

        override suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean): DomainResult<Unit> {
            toggled += book.key to isFavorite
            keys.value = if (isFavorite) keys.value - book.key else keys.value + book.key
            return DomainResult.Success(Unit)
        }
    }
}
