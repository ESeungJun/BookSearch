package data.search.source.remote

import data.search.data.SearchBookApi
import data.search.service.ISearchBookService
import domain.base.data.DomainResult
import domain.search.data.SearchSort
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** 통신 결과가 성공·실패(상태 코드)·에러(원인 예외)로 나뉘는지 확인한다. */
class SearchRemoteDataSourceImplTest {
    private val service = FakeService()
    private val remote = SearchRemoteDataSourceImpl(service)

    @Test
    fun `연결 실패는 IOException 을 담은 에러다`() = runTest {
        service.error = IOException()
        assertTrue((search() as DomainResult.Error).cause is IOException)
    }

    @Test
    fun `HTTP 오류는 상태 코드만 담은 실패다(응답 본문은 넘기지 않는다)`() = runTest {
        service.error = HttpException(Response.error<SearchBookApi>(401, "key".toResponseBody()))
        assertEquals(DomainResult.Fail(401), search())
    }

    @Test
    fun `목록이나 총 개수가 없는 응답은 빈 결과가 아니라 에러다`() = runTest {
        service.response = SearchBookApi(meta = null, documents = null)
        assertTrue(search() is DomainResult.Error)
    }

    private suspend fun search() = remote.searchBooks("kotlin", SearchSort.ACCURACY, 1)

    private class FakeService : ISearchBookService {
        var error: Exception? = null
        var response: SearchBookApi? = null
        override suspend fun search(query: String, sort: String, page: Int, size: Int): SearchBookApi = error?.let { throw it } ?: response!!
    }
}
