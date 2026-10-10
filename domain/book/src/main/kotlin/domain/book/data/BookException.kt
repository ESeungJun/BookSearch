package domain.book.data

/**
 * 데이터를 가져오지 못한 이유. 화면은 이 [reason] 만 보고 문구를 고른다.
 * Retrofit·OkHttp 예외를 그대로 올리면 화면 모듈이 네트워크 라이브러리를 알아야 해서 여기서 한 번 바꾼다.
 */
class BookException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason {
        NETWORK, // 연결 없음·3초 초과
        AUTH, // API 키 오류(401·403)
        SERVER, // 5xx·429·응답 형식 오류
    }
}
