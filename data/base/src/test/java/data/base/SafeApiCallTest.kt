package data.base

import domain.base.data.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** 서버 호출 결과가 성공·실패(상태 코드)·에러(원인 예외)로 나뉘고, 취소는 그대로 전달되는지 확인한다. */
class SafeApiCallTest {

    @Test
    fun `값을 돌려주면 성공이다`() = runTest {
        assertEquals(DomainResult.Success(1), safeApiCall { 1 })
    }

    @Test
    fun `HTTP 오류는 상태 코드만 담은 실패다`() = runTest {
        val result = safeApiCall<Int> { throw HttpException(Response.error<Int>(401, "key".toResponseBody())) }
        assertEquals(DomainResult.Fail(401), result)
    }

    @Test
    fun `연결 실패는 IOException 을 담은 에러다`() = runTest {
        val result = safeApiCall<Int> { throw IOException() } as DomainResult.Error
        assertTrue(result.cause is IOException)
    }

    @Test(expected = CancellationException::class)
    fun `취소는 결과로 바꾸지 않고 그대로 던진다`() = runTest {
        safeApiCall<Int> { throw CancellationException() }
    }
}
