package presentation.base

import androidx.annotation.StringRes
import domain.base.data.DomainResult
import java.io.IOException

/** 키 오류(401·403). 다시 시도해도 같은 결과라 다른 실패와 따로 알린다. */
fun DomainResult<*>.isServiceConfigError(): Boolean =
    this is DomainResult.Fail && code in SERVICE_CONFIG_CODES

/**
 * 실패·에러를 사용자에게 보일 문구로 바꾼다. HTTP 코드와 예외 종류만 보고, 예외 메시지는 쓰지 않는다
 * (서버 응답이나 요청 정보가 섞여 있을 수 있다). 성공을 넘기면 마지막 분기의 문구가 나온다.
 */
@StringRes
fun DomainResult<*>.failureMessageRes(): Int = when {
    isServiceConfigError() -> R.string.result_service_config
    this is DomainResult.Error && cause is IOException -> R.string.result_connection
    else -> R.string.result_retry_later
}

private val SERVICE_CONFIG_CODES = setOf(401, 403)
