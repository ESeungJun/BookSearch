package data.base

import domain.base.data.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** DB 호출 하나를 [DomainResult] 로 바꾼다. DB 오류는 원인 예외를 [DomainResult.Error] 에 담고, 취소는 그대로 던진다. */
suspend fun <T> safeDbCall(block: suspend () -> T): DomainResult<T> =
    try {
        DomainResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DomainResult.Error(e)
    }

/** DB 를 계속 관찰하는 값을 [DomainResult] 로 바꾼다. 오류가 나면 Error 를 한 번 내보내고 끝난다. */
fun <T> Flow<T>.safeDbFlow(): Flow<DomainResult<T>> =
    map<T, DomainResult<T>> { DomainResult.Success(it) }
        .catch { e -> if (e is CancellationException) throw e else emit(DomainResult.Error(e)) }
