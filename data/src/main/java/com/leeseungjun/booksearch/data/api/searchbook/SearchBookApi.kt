package com.leeseungjun.booksearch.data.api.searchbook

import retrofit2.http.GET
import retrofit2.http.Query

/** GET /v3/search/book — 계약과 실측은 docs/api.md */
interface SearchBookApi {
    // Retrofit 의 suspend 함수는 OkHttp 스레드에서 실행되므로 호출하는 쪽에서 디스패처를 바꾸지 않아도 된다
    @GET("v3/search/book")
    suspend fun search(
        @Query("query") query: String,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): SearchBookResponse
}
