package domain.search.repo

import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort

interface ISearchRepository {
    /** 네트워크를 먼저 시도하고, 3초를 넘기거나 실패하면 저장해 둔 결과를 돌려준다. 그것도 없으면 [domain.book.data.BookException]. */
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO>

    /** 결과를 받아 저장한 마지막 검색. 저장된 검색이 없으면 null. */
    suspend fun getLastSearch(): SearchConditionDTO?
}
