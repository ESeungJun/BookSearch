package data.search.source.local

import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

/** 검색 결과 캐시만 맡는다. 무엇을 얼마나 남길지(캐시 보관 규칙)도 여기서 정한다. */
interface ISearchLocalDataSource {
    suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO)
    suspend fun getSearchPage(query: String, sort: SearchSort, page: Int): SearchPageDTO?
    suspend fun getLastSearch(): SearchConditionDTO?
}
