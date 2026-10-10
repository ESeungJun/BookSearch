package data.repo

import data.source.local.IBookLocalDataSource
import data.source.remote.IBookRemoteDataSource
import domain.data.BookDTO
import domain.data.BookException
import domain.data.BookException.Reason
import domain.data.SearchConditionDTO
import domain.data.SearchPageDTO
import domain.data.SearchSort
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** 저장소의 "네트워크 먼저 → 3초 넘거나 실패하면 저장해 둔 결과" 분기만 확인한다. 캐시 보관 규칙은 로컬 데이터 소스 테스트에서 본다. */
class BookRepositoryImplTest {
    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val repository = BookRepositoryImpl(remote, local)

    @Test
    fun `성공하면 결과를 돌려주고 로컬에 저장한다`() = runTest {
        val page = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).getOrThrow()
        assertNull(page.cachedAt)
        assertEquals(PAGE, local.saved["kotlin|1"])
    }

    @Test
    fun `3초를 넘기면 저장해 둔 결과를 돌려준다`() = runTest {
        local.saved["kotlin|1"] = PAGE.copy(cachedAt = 1L)
        remote.delayMs = 5_000
        val page = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).getOrThrow()
        assertNotNull(page.cachedAt)
    }

    @Test
    fun `실패하고 저장해 둔 결과도 없으면 원격의 실패 이유를 그대로 돌려준다`() = runTest {
        remote.error = BookException(Reason.AUTH)
        val error = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).exceptionOrNull()
        assertEquals(Reason.AUTH, (error as BookException).reason)
    }

    @Test
    fun `3초를 넘기고 저장해 둔 결과도 없으면 NETWORK 실패다`() = runTest {
        remote.delayMs = 5_000
        val error = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).exceptionOrNull()
        assertEquals(Reason.NETWORK, (error as BookException).reason)
    }

    private class FakeRemote : IBookRemoteDataSource {
        var delayMs = 0L
        var error: BookException? = null
        override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): SearchPageDTO {
            delay(delayMs)
            error?.let { throw it }
            return PAGE
        }
    }

    private class FakeLocal : IBookLocalDataSource {
        val saved = mutableMapOf<String, SearchPageDTO>()
        override suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO) {
            saved["$query|$page"] = result
        }
        override suspend fun getSearchPage(query: String, sort: SearchSort, page: Int) = saved["$query|$page"]
        override suspend fun getLastSearch(): SearchConditionDTO? = null
        override suspend fun getBook(key: String): BookDTO? = null
        override fun observeFavorites(): Flow<List<BookDTO>> = emptyFlow()
        override suspend fun addFavorite(book: BookDTO) = Unit
        override suspend fun removeFavorite(key: String) = Unit
    }

    private companion object {
        val PAGE = SearchPageDTO(books = emptyList(), totalCount = 0, isEnd = true)
    }
}
