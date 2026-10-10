package presentation.search.main

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import domain.favorite.usecase.ObserveFavoriteKeysUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import domain.search.usecase.GetLastSearchUseCase
import domain.search.usecase.LoadMoreBooksUseCase
import domain.search.usecase.SearchBooksUseCase
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import presentation.base.R as BaseR

/** 화면에 조용히 틀리게 보일 수 있는 판단만 확인한다: 디바운스·취소·페이징·캐시 안내·토스트·하트 결합. */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val search = FakeSearchRepository()
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
    fun `입력이 멈추고 400ms 뒤에 마지막 검색어로 한 번만 검색한다`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("코")
        viewModel.onQueryChange("코틀")
        viewModel.onQueryChange("코틀린 ")
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS - 1)
        runCurrent()
        assertTrue(search.calls.isEmpty())

        advanceUntilIdle()
        assertEquals(listOf(Call("코틀린", SearchSort.ACCURACY, 1)), search.calls)
    }

    @Test
    fun `공백만 입력하면 호출 없이 첫 진입 상태다`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("   ")
        advanceUntilIdle()
        assertTrue(search.calls.isEmpty())
        assertEquals(SearchStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `검색어를 비우면 결과를 지우고 첫 진입 상태로 돌아간다`() = runTest {
        search.pages["코틀린"] = page(books("a", 3))
        val viewModel = searched("코틀린")
        viewModel.onQueryChange("")
        assertEquals(SearchStatus.Idle, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.books.isEmpty())
    }

    @Test
    fun `검색어가 바뀌면 늦게 온 이전 응답이 새 결과를 덮지 않는다`() = runTest {
        search.delayMs["느린"] = 5_000
        search.pages["느린"] = page(books("old", 1))
        search.pages["빠른"] = page(books("new", 1))
        val viewModel = viewModel()
        viewModel.onQueryChange("느린")
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS + 1) // 느린 요청이 진행 중
        viewModel.onQueryChange("빠른")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("빠른", state.searchedQuery)
        assertEquals(listOf("new0"), state.books.map { it.key })
    }

    @Test
    fun `다음 페이지를 이어 붙이고 isEnd 면 더 요청하지 않는다`() = runTest {
        search.pages["코틀린"] = page(books("a", 20), isEnd = false)
        val viewModel = searched("코틀린")
        search.pages["코틀린"] = page(books("b", 5), isEnd = true)

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(25, viewModel.uiState.value.books.size)
        assertEquals(LoadMoreState.END, viewModel.uiState.value.loadMore)

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), search.calls.map { it.page })
    }

    @Test
    fun `다음 페이지가 실패해도 받은 목록은 남고 끝에서 다시 시도할 수 있다`() = runTest {
        search.pages["코틀린"] = page(books("a", 20), isEnd = false)
        val viewModel = searched("코틀린")
        search.failure = DomainResult.Error(IOException())

        viewModel.loadMore()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(SearchStatus.Results, state.status)
        assertEquals(20, state.books.size)
        assertEquals(LoadMoreState.FAILED, state.loadMore)

        search.failure = null
        search.pages["코틀린"] = page(books("b", 3), isEnd = true)
        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(23, viewModel.uiState.value.books.size)
    }

    @Test
    fun `다음 페이지에 이미 받은 책이 다시 오면 빼고 끝으로 본다`() = runTest {
        search.pages["코틀린"] = page(books("a", 20), isEnd = false)
        val viewModel = searched("코틀린")

        viewModel.loadMore() // 같은 페이지가 다시 온다
        advanceUntilIdle()
        assertEquals(20, viewModel.uiState.value.books.size)
        assertEquals(LoadMoreState.END, viewModel.uiState.value.loadMore)
    }

    @Test
    fun `정렬을 바꾸면 같은 검색어로 1페이지부터 다시 받는다`() = runTest {
        val viewModel = searched("코틀린")
        viewModel.onSortChange(SearchSort.LATEST)
        advanceUntilIdle()
        assertEquals(Call("코틀린", SearchSort.LATEST, 1), search.calls.last())
    }

    @Test
    fun `저장된 결과로 대신 보여 주면 안내 줄을 띄우고, 키 오류면 토스트를 한 번 보인다`() = runTest {
        search.pages["코틀린"] = page(books("a", 3), cachedAt = 1_700_000_000_000, failure = DomainResult.Fail(401))
        val viewModel = searched("코틀린")

        val state = viewModel.uiState.value
        assertNotNull(state.cachedTime)
        assertEquals(BaseR.string.result_service_config, state.toastRes)
        viewModel.onToastShown()
        assertNull(viewModel.uiState.value.toastRes)
    }

    @Test
    fun `저장된 결과를 보여 줘도 키 오류가 아니면 토스트는 없다`() = runTest {
        search.pages["코틀린"] = page(books("a", 3), cachedAt = 1L, failure = DomainResult.Error(IOException()))
        val viewModel = searched("코틀린")
        assertNotNull(viewModel.uiState.value.cachedTime)
        assertNull(viewModel.uiState.value.toastRes)
    }

    @Test
    fun `저장된 결과도 없으면 원인별 문구의 오류 화면이다`() = runTest {
        search.failure = DomainResult.Error(IOException())
        assertEquals(SearchStatus.Error(BaseR.string.result_connection), searched("a").uiState.value.status)

        search.failure = DomainResult.Fail(403)
        assertEquals(SearchStatus.Error(BaseR.string.result_service_config), searched("b").uiState.value.status)

        search.failure = DomainResult.Fail(500)
        assertEquals(SearchStatus.Error(BaseR.string.result_retry_later), searched("c").uiState.value.status)
    }

    @Test
    fun `하트는 즐겨찾기 키에서 계산하고 바뀌면 바로 반영된다`() = runTest {
        search.pages["코틀린"] = page(books("a", 2))
        favorite.keys.value = setOf("a0")
        val viewModel = searched("코틀린")
        assertEquals(listOf(true, false), viewModel.uiState.value.books.map { it.isFavorite })

        favorite.keys.value = setOf("a1")
        advanceUntilIdle()
        assertEquals(listOf(false, true), viewModel.uiState.value.books.map { it.isFavorite })
    }

    @Test
    fun `하트를 누르면 지금 상태와 함께 토글한다`() = runTest {
        search.pages["코틀린"] = page(books("a", 2))
        favorite.keys.value = setOf("a0")
        val viewModel = searched("코틀린")

        viewModel.onFavoriteClick("a0")
        advanceUntilIdle()
        assertEquals("a0" to true, favorite.toggled.single())
    }

    @Test
    fun `앱을 열면 마지막 검색어와 정렬로 검색한다`() = runTest {
        search.last = SearchConditionDTO("코틀린", SearchSort.LATEST)
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals("코틀린", viewModel.uiState.value.query)
        assertEquals(listOf(Call("코틀린", SearchSort.LATEST, 1)), search.calls)
    }

    private fun viewModel() = SearchViewModel(
        searchBooksUseCase = SearchBooksUseCase(search),
        loadMoreBooksUseCase = LoadMoreBooksUseCase(search),
        toggleFavoriteUseCase = ToggleFavoriteUseCase(favorite),
        getLastSearchUseCase = GetLastSearchUseCase(search),
        observeFavoriteKeysUseCase = ObserveFavoriteKeysUseCase(favorite),
    )

    private fun TestScope.searched(query: String): SearchViewModel {
        val viewModel = viewModel()
        viewModel.onQueryChange(query)
        advanceUntilIdle()
        return viewModel
    }

    private fun books(prefix: String, count: Int) = List(count) { book("$prefix$it") }

    private fun page(
        books: List<BookDTO>,
        isEnd: Boolean = true,
        cachedAt: Long? = null,
        failure: DomainResult<Nothing>? = null,
    ) = SearchPageDTO(books, totalCount = 100, isEnd = isEnd, cachedAt = cachedAt, failure = failure)

    private fun book(key: String) = BookDTO(
        key = key, title = key, authors = null, publisher = null, publishedDate = null,
        price = null, salePrice = null, thumbnailUrl = null, isbn = null, description = null, url = null,
    )

    private data class Call(val query: String, val sort: SearchSort, val page: Int)

    private class FakeSearchRepository : ISearchRepository {
        val pages = mutableMapOf<String, SearchPageDTO>()
        val delayMs = mutableMapOf<String, Long>()
        val calls = mutableListOf<Call>()
        var failure: DomainResult<Nothing>? = null
        var last: SearchConditionDTO? = null

        override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> {
            calls += Call(query, sort, page)
            delay(delayMs[query] ?: 0L)
            failure?.let { return it }
            return DomainResult.Success(pages[query] ?: SearchPageDTO(emptyList(), totalCount = 0, isEnd = true))
        }

        override suspend fun getLastSearch(): DomainResult<SearchConditionDTO?> = DomainResult.Success(last)
    }

    private class FakeFavoriteRepository : IFavoriteRepository {
        val keys = MutableStateFlow<Set<String>>(emptySet())
        val toggled = mutableListOf<Pair<String, Boolean>>()

        override fun observeFavorites(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> =
            throw UnsupportedOperationException()

        override fun observeFavoriteKeys(): Flow<DomainResult<Set<String>>> = keys.map { DomainResult.Success(it) }

        override suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean): DomainResult<Unit> {
            toggled += book.key to isFavorite
            return DomainResult.Success(Unit)
        }
    }
}
