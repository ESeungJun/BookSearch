package data.api.searchbook.service

import data.api.searchbook.data.SearchBookApi
import retrofit2.http.GET
import retrofit2.http.Query

/** 카카오 도서 검색 GET /v3/search/book. 마지막 페이지를 넘겨 요청하면 마지막 페이지를 다시 돌려준다(is_end = true). */
interface ISearchBookService {
    // Retrofit 의 suspend 함수는 OkHttp 스레드에서 실행되므로 호출하는 쪽에서 디스패처를 바꾸지 않아도 된다
    @GET("v3/search/book")
    suspend fun search(
        @Query("query") query: String,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): SearchBookApi
}
