package domain.base

import domain.base.data.DomainResult
import domain.base.usecase.asUseCaseResult
import domain.base.usecase.useCase
import domain.base.usecase.useCaseResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** UseCase 공통 결과 처리가 값·예외·취소·Flow 를 어떻게 바꾸는지 확인한다. */
class UseCaseResultTest {

    @Test
    fun `값은 성공으로 감싼다`() = runTest {
        assertEquals(DomainResult.Success(1), useCase { 1 })
    }

    @Test
    fun `예외는 에러로 바꾼다`() = runTest {
        assertTrue(useCase<Int> { throw IllegalStateException() } is DomainResult.Error)
        assertTrue(useCaseResult<Int> { throw IllegalStateException() } is DomainResult.Error)
    }

    @Test
    fun `이미 결과인 값은 그대로 둔다`() = runTest {
        assertEquals(DomainResult.Fail(401), useCaseResult<Int> { DomainResult.Fail(401) })
    }

    @Test(expected = CancellationException::class)
    fun `취소는 결과로 바꾸지 않고 그대로 던진다`() = runTest {
        useCase<Int> { throw CancellationException() }
    }

    @Test
    fun `Flow 의 값은 성공, 예외는 마지막 에러가 된다`() = runTest {
        val results = flow { emit(1); throw IllegalStateException() }.asUseCaseResult().toList()
        assertEquals(DomainResult.Success(1), results[0])
        assertTrue(results[1] is DomainResult.Error)
    }
}
