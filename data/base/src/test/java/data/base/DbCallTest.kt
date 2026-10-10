package data.base

import domain.base.data.DomainResult
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** DB 호출 결과가 성공·에러로 나뉘는지 확인한다. 취소 처리는 apiCall 과 같다. */
class DbCallTest {

    @Test
    fun `값은 성공, 예외는 에러다`() = runTest {
        assertEquals(DomainResult.Success(1), dbCall { 1 })
        assertTrue(dbCall<Int> { throw IllegalStateException() } is DomainResult.Error)
    }

    @Test
    fun `관찰 중 예외는 마지막 에러가 된다`() = runTest {
        val results = flow { emit(1); throw IllegalStateException() }.asDbResult().toList()
        assertEquals(DomainResult.Success(1), results[0])
        assertTrue(results[1] is DomainResult.Error)
    }
}
