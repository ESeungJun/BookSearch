package domain.search.repo

import domain.base.data.DomainResult
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

interface ISearchRepository {
    /** 네트워크를 먼저 시도하고, 3초를 넘기거나 실패하면 저장해 둔 결과를 성공으로 돌려준다(cachedAt 이 있음). 그것도 없으면 실패·에러. */
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO>

    /** 결과를 받아 저장한 마지막 검색. 저장된 검색이 없으면 Success(null). */
    suspend fun getLastSearch(): DomainResult<SearchConditionDTO?>
}
