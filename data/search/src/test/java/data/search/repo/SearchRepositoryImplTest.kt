package data.search.repo

import data.search.source.local.ISearchLocalDataSource
import data.search.source.remote.ISearchRemoteDataSource
import domain.book.data.DomainResult
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

/** 저장소의 "네트워크 먼저 → 3초 넘거나 실패하면 저장해 둔 결과" 분기만 확인한다. 캐시 보관 규칙은 로컬 데이터 소스 테스트에서 본다. */
class SearchRepositoryImplTest {
    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val repository = SearchRepositoryImpl(remote, local)

    @Test
    fun `성공하면 결과를 돌려주고 로컬에 저장한다`() = runTest {
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1)
        assertEquals(DomainResult.Success(PAGE), result)
        assertEquals(PAGE, local.saved["kotlin|1"])
    }

    @Test
    fun `실패해도 저장해 둔 결과가 있으면 그것을 성공으로 돌려준다`() = runTest {
        local.saved["kotlin|1"] = PAGE.copy(cachedAt = 1L)
        remote.result = DomainResult.Fail(500)
        val result = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1) as DomainResult.Success
        assertNotNull(result.data.cachedAt)
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

    private class FakeRemote : ISearchRemoteDataSource {
        var delayMs = 0L
        var result: DomainResult<SearchPageDTO> = DomainResult.Success(PAGE)
        override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> {
            delay(delayMs)
            return result
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
