package data.api.searchbook.repo

import data.api.searchbook.data.SearchBookApi
import data.api.searchbook.source.local.ISearchLocalDataSource
import data.api.searchbook.source.remote.ISearchRemoteDataSource
import data.base.db.entity.BookEntity
import data.base.db.entity.SearchCacheEntity
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import java.net.SocketTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 저장소의 "네트워크 먼저 → 3초 넘거나 실패하면 저장해 둔 결과" 분기만 확인한다. 캐시 보관 규칙은 로컬 데이터 소스 테스트에서 본다. */
class SearchRepositoryImplTest {
    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val repository = SearchRepositoryImpl(remote, local)

    @Test
    fun `성공하면 결과를 돌려주고 로컬에 저장한다`() = runTest {
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1)
        assertEquals(DomainResult.Success(PAGE), result)
        assertTrue("kotlin|1" in local.savedPages)
    }

    @Test
    fun `실패해도 저장해 둔 결과가 있으면 그것을 성공으로 돌려준다`() = runTest {
        local.cachedRows = listOf(SearchCacheEntity("kotlin", "ACCURACY", 1, 0, "A", 1, true, savedAt = 1L))
        remote.result = DomainResult.Fail(500)
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1) as DomainResult.Success
        assertNotNull(result.data.cachedAt)
    }

    @Test
    fun `같은 검색의 저장 결과가 없으면 저장된 책에서 찾은 결과를 돌려준다`() = runTest {
        local.savedBooks = listOf(BOOK)
        remote.result = DomainResult.Error(java.io.IOException())
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1) as DomainResult.Success
        assertTrue(result.data.isLocalMatch && result.data.isEnd)
    }

    @Test
    fun `다음 페이지는 저장된 책에서 찾지 않고 실패를 그대로 돌려준다`() = runTest {
        local.savedBooks = listOf(BOOK)
        remote.result = DomainResult.Fail(500)
        assertEquals(DomainResult.Fail(500), repository.searchBooks("kotlin", SearchSort.ACCURACY, 2))
    }

    @Test
    fun `실패하고 저장해 둔 결과도 없으면 원격의 결과를 그대로 돌려준다`() = runTest {
        remote.result = DomainResult.Fail(401)
        assertEquals(DomainResult.Fail(401), repository.searchBooks("kotlin", SearchSort.ACCURACY, 1))
    }

    @Test
    fun `3초를 넘기고 저장해 둔 결과도 없으면 연결 문제(IOException) 에러다`() = runTest {
        remote.delayMs = 5_000
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1) as DomainResult.Error
        assertTrue(result.cause is SocketTimeoutException)
    }

    @Test
    fun `목록이나 총 개수가 없는 응답은 빈 결과가 아니라 에러다`() = runTest {
        remote.result = DomainResult.Success(SearchBookApi(meta = null, documents = null))
        assertTrue(repository.searchBooks("kotlin", SearchSort.ACCURACY, 1) is DomainResult.Error)
    }

    private class FakeRemote : ISearchRemoteDataSource {
        var delayMs = 0L
        var result: DomainResult<SearchBookApi> = DomainResult.Success(RESPONSE)
        override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchBookApi> {
            delay(delayMs)
            return result
        }
    }

    private class FakeLocal : ISearchLocalDataSource {
        val savedPages = mutableSetOf<String>()
        var cachedRows = emptyList<SearchCacheEntity>()
        var savedBooks = emptyList<BookEntity>()
        override suspend fun saveSearchPage(
            query: String,
            sort: SearchSort,
            page: Int,
            books: List<BookEntity>,
            totalCount: Int,
            isEnd: Boolean,
        ) {
            savedPages += "$query|$page"
        }
        override suspend fun getCachedRows(query: String, sort: SearchSort, page: Int) = cachedRows
        override suspend fun getCachedBooks(query: String, sort: SearchSort, page: Int) = emptyList<BookEntity>()
        override suspend fun findSavedBooks(query: String, sort: SearchSort) = savedBooks
    }

    private companion object {
        val RESPONSE = SearchBookApi(SearchBookApi.MetaApi(totalCount = 0, isEnd = true), documents = emptyList())
        val PAGE = SearchPageDTO(books = emptyList(), totalCount = 0, isEnd = true)
        val BOOK = BookEntity("A", "A", emptyList(), "", "", null, null, null, "", "", "")
    }
}
