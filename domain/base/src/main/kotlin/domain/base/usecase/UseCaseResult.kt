package domain.base.usecase

import domain.base.data.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

// UseCase 가 결과를 돌려줄 때 쓰는 공통 처리. 저장소는 DomainResult 를 돌려주지만, 그 밖으로 예외가 새어 나와도
// DomainResult.Error 로 바꿔 화면까지 예외가 가지 않게 한다. 취소는 실패가 아니므로 그대로 던진다.

/** 저장소 호출의 결과를 그대로 돌려주고, 밖으로 던진 예외만 [DomainResult.Error] 로 바꾼다. */
suspend fun <T> useCaseResult(block: suspend () -> DomainResult<T>): DomainResult<T> =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DomainResult.Error(e)
    }

/** 관찰하는 결과에서 밖으로 던진 예외를 [DomainResult.Error] 한 번으로 바꾸고 끝낸다. */
fun <T> Flow<DomainResult<T>>.asUseCaseResult(): Flow<DomainResult<T>> =
    catch { e -> if (e is CancellationException) throw e else emit(DomainResult.Error(e)) }
