package presentation.base.data

import androidx.annotation.StringRes
import domain.base.data.DomainResult
import java.io.IOException
import presentation.base.R

/** 키 오류(401·403). 다시 시도해도 같은 결과라 다른 실패와 따로 알린다. */
fun DomainResult<*>.isServiceConfigError(): Boolean = when (this) {
    is DomainResult.Fail -> code in SERVICE_CONFIG_CODES
    is DomainResult.Success, is DomainResult.Error -> false
}

/**
 * 실패·에러를 사용자에게 보일 문구로 바꾼다. HTTP 코드와 예외 종류만 보고, 예외 메시지는 쓰지 않는다
 * (서버 응답이나 요청 정보가 섞여 있을 수 있다).
 */
@StringRes
fun DomainResult<*>.failureMessageRes(): Int = when (this) {
    is DomainResult.Fail -> if (code in SERVICE_CONFIG_CODES) R.string.result_service_config else R.string.result_retry_later
    is DomainResult.Error -> if (cause is IOException) R.string.result_connection else R.string.result_retry_later
    // 성공에는 보일 실패 문구가 없다. 잘못 넘겨도 화면이 깨지지 않게 일반 문구를 준다
    is DomainResult.Success -> R.string.result_retry_later
}

private val SERVICE_CONFIG_CODES = setOf(401, 403)
