package data.api.searchbook.source.local

import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

/** 검색 결과 캐시와, 네트워크가 실패했을 때 저장된 모든 책에서 찾기를 맡는다. 무엇을 얼마나 남길지(캐시 보관 규칙)도 여기서 정한다. */
interface ISearchLocalDataSource {
    suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO)
    suspend fun getSearchPage(query: String, sort: SearchSort, page: Int): SearchPageDTO?
    /** 저장된 모든 책에서 검색어로 찾는다. 하나도 없으면 null. */
    suspend fun findSavedBooks(query: String, sort: SearchSort): SearchPageDTO?
}
