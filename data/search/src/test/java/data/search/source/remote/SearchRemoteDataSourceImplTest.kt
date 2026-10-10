package data.search.source.remote

import data.search.data.SearchBookApi
import data.search.service.ISearchBookService
import domain.book.data.BookException
import domain.book.data.BookException.Reason
import domain.search.data.SearchSort
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** 통신 실패가 화면이 구분할 수 있는 이유(NETWORK·AUTH·SERVER)로 바뀌는지 확인한다. */
class SearchRemoteDataSourceImplTest {
    private val service = FakeService()
    private val remote = SearchRemoteDataSourceImpl(service)

    @Test
    fun `연결 실패는 NETWORK 다`() = runTest {
        service.error = IOException()
        assertEquals(Reason.NETWORK, reasonOf())
    }

    @Test
    fun `401 은 AUTH 이고 응답 본문(키 일부)을 원인으로 붙이지 않는다`() = runTest {
        service.error = HttpException(Response.error<SearchBookApi>(401, "key".toResponseBody()))
        val error = runCatching { remote.searchBooks("kotlin", SearchSort.ACCURACY, 1) }.exceptionOrNull() as BookException
        assertEquals(Reason.AUTH, error.reason)
        assertEquals(null, error.cause)
    }

    @Test
    fun `500 은 SERVER 다`() = runTest {
        service.error = HttpException(Response.error<SearchBookApi>(500, "".toResponseBody()))
        assertEquals(Reason.SERVER, reasonOf())
    }

    @Test
    fun `목록이나 총 개수가 없는 응답은 빈 결과가 아니라 SERVER 다`() = runTest {
        service.response = SearchBookApi(meta = null, documents = null)
        assertEquals(Reason.SERVER, reasonOf())
    }

    private suspend fun reasonOf(): Reason =
        (runCatching { remote.searchBooks("kotlin", SearchSort.ACCURACY, 1) }.exceptionOrNull() as BookException).reason

    private class FakeService : ISearchBookService {
        var error: Exception? = null
        var response: SearchBookApi? = null
        override suspend fun search(query: String, sort: String, page: Int, size: Int): SearchBookApi = error?.let { throw it } ?: response!!
    }
}
