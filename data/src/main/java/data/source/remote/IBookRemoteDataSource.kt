package data.source.remote

import domain.data.BookException
import domain.data.SearchPageDTO
import domain.data.SearchSort

/** 서버 호출만 맡는다. 실패는 [BookException] 으로 바꿔 던져서 저장소가 Retrofit 을 몰라도 되게 한다. */
interface IBookRemoteDataSource {
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): SearchPageDTO
}
