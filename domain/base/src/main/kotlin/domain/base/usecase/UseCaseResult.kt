package domain.base.usecase

import domain.base.data.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// UseCase 가 결과를 돌려줄 때 쓰는 공통 처리. 저장소에서 올라온 예상하지 못한 예외(DB 오류 등)를
// DomainResult.Error 로 바꿔 화면까지 예외가 새지 않게 한다. 취소는 실패가 아니므로 그대로 던진다.

/** 값을 돌려주는 저장소 호출을 결과로 감싼다. */
suspend fun <T> useCase(block: suspend () -> T): DomainResult<T> =
    try {
        DomainResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DomainResult.Error(e)
    }

/** 이미 [DomainResult] 를 돌려주는 저장소 호출에서, 그 밖으로 던진 예외만 결과로 바꾼다. */
suspend fun <T> useCaseResult(block: suspend () -> DomainResult<T>): DomainResult<T> =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DomainResult.Error(e)
    }

/** 계속 관찰하는 값을 결과로 감싼다. 예외가 나면 Error 를 한 번 내보내고 끝난다. */
fun <T> Flow<T>.asUseCaseResult(): Flow<DomainResult<T>> =
    map<T, DomainResult<T>> { DomainResult.Success(it) }
        .catch { e -> if (e is CancellationException) throw e else emit(DomainResult.Error(e)) }
