package data.search.repo

import data.search.source.local.ISearchLocalDataSource
import data.search.source.remote.ISearchRemoteDataSource
import domain.book.data.BookException
import domain.book.data.BookException.Reason
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** 저장소의 "네트워크 먼저 → 3초 넘거나 실패하면 저장해 둔 결과" 분기만 확인한다. 캐시 보관 규칙은 로컬 데이터 소스 테스트에서 본다. */
class SearchRepositoryImplTest {
    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val repository = SearchRepositoryImpl(remote, local)

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

    private class FakeRemote : ISearchRemoteDataSource {
        var delayMs = 0L
        var error: BookException? = null
        override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): SearchPageDTO {
            delay(delayMs)
            error?.let { throw it }
            return PAGE
        }
    }

    private class FakeLocal : ISearchLocalDataSource {
        val saved = mutableMapOf<String, SearchPageDTO>()
        override suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO) {
            saved["$query|$page"] = result
        }
        override suspend fun getSearchPage(query: String, sort: SearchSort, page: Int) = saved["$query|$page"]
        override suspend fun getLastSearch(): SearchConditionDTO? = null
    }

    private companion object {
        val PAGE = SearchPageDTO(books = emptyList(), totalCount = 0, isEnd = true)
    }
}
