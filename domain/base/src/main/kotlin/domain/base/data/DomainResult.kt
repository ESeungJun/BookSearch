package domain.base.data

/**
 * domain 이 돌려주는 결과. **성공·실패·에러라는 상황만** 전하고, 왜 그런지는 정하지 않는다(D-63).
 * 구체적인 원인은 보내는 쪽(data)이 채우고, 어떻게 보여 줄지는 받는 쪽(presentation)이 판단한다.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>

    /** 서버가 응답했지만 성공이 아니다. [code] 는 HTTP 상태 코드다. 응답 본문은 담지 않는다(401 본문에 키 일부가 있다). */
    data class Fail(val code: Int) : DomainResult<Nothing>

    /** 쓸 수 있는 응답을 받지 못했다(연결 실패·시간 초과·형식 오류). 연결 문제는 [cause] 가 `IOException` 이다. */
    data class Error(val cause: Throwable) : DomainResult<Nothing>
}
