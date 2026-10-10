package data.base

import domain.base.data.DomainResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/**
 * 서버 호출 하나를 [DomainResult] 로 바꾼다. [block] 이 응답을 DTO 로 바꾸는 것까지 하면,
 * 그 과정에서 던진 예외(필수 필드 누락 등)도 [DomainResult.Error] 가 된다.
 *
 * - 취소는 실패가 아니므로 잡지 않고 그대로 던진다(화면을 떠나거나 새 검색으로 이전 요청이 취소된 경우).
 * - HTTP 오류는 상태 코드만 [DomainResult.Fail] 에 담는다. 401 응답 본문에 API 키 일부가 들어 있어 예외를 넘기지 않는다.
 * - 그 밖의 실패(연결 실패·형식 오류)는 원인 예외를 [DomainResult.Error] 에 담는다.
 */
suspend fun <T> apiCall(block: suspend () -> T): DomainResult<T> =
    try {
        DomainResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        DomainResult.Fail(e.code())
    } catch (e: Exception) {
        DomainResult.Error(e)
    }
