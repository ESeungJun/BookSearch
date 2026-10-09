package com.leeseungjun.booksearch.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 일시적인 서버 오류(5xx·429)를 사용자 모르게 짧게 다시 시도한다.
 *
 * 왜 여기서: HTTP 상태 코드를 아는 곳은 통신 계층뿐이다. 화면이나 UseCase 가 재시도하려면
 * HTTP 를 알아야 하거나, 데이터 계층이 "재시도하라"는 앱 정책을 알아야 한다. 같은 GET 을 다시 보내는 것은
 * 결과가 바뀌지 않는 전송 문제라 통신 계층 안에서 끝낸다. 화면에는 최종 실패만 올라간다.
 * 저장소의 3초 제한 안에서 끝나도록 횟수와 간격을 작게 둔다.
 */
class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        var attempt = 0
        while (response.isRetryable() && attempt < MAX_RETRIES && !chain.call().isCanceled()) {
            response.close()
            Thread.sleep(RETRY_DELAY_MS) // OkHttp 작업 스레드라 메인 스레드를 막지 않는다
            attempt++
            response = chain.proceed(chain.request())
        }
        return response
    }

    // GET 만 다시 보낸다 — 같은 요청을 여러 번 보내도 결과가 같아야 재시도해도 안전하다
    private fun Response.isRetryable(): Boolean = request.method == "GET" && (code == 429 || code in 500..599)

    companion object {
        private const val MAX_RETRIES = 2
        private const val RETRY_DELAY_MS = 300L
    }
}
