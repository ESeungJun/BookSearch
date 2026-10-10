package data.search.source.remote

import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

/** 서버 호출만 맡는다. 실패를 [DomainResult] 로 바꿔 돌려주므로 저장소는 Retrofit 을 몰라도 된다. */
interface ISearchRemoteDataSource {
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO>
}
