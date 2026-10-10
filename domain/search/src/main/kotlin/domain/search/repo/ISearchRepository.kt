package domain.search.repo

import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

interface ISearchRepository {
    /**
     * 네트워크를 먼저 시도하고, 3초를 넘기거나 실패하면 같은 검색의 저장 결과(cachedAt), 그것도 없으면 첫 페이지에 한해
     * 기기에 저장된 책에서 찾은 결과(isLocalMatch)를 성공으로 돌려준다. 어느 것도 없으면 실패·에러.
     */
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO>
}
