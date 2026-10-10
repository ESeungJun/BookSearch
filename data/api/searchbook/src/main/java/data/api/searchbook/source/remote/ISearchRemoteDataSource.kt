package data.api.searchbook.source.remote

import data.api.searchbook.data.SearchBookApi
import domain.base.data.DomainResult
import domain.search.data.SearchSort

/** 서버 호출만 맡는다. 응답은 서버 모양([SearchBookApi]) 그대로 주고, 실패만 [DomainResult] 로 바꿔 저장소가 Retrofit 을 몰라도 되게 한다. */
interface ISearchRemoteDataSource {
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchBookApi>
}
