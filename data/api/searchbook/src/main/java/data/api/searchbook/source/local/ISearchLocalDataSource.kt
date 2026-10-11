package data.api.searchbook.source.local

import data.api.searchbook.data.CachedPage
import data.base.db.entity.BookEntity
import domain.search.data.SearchSort

/**
 * 검색 결과 캐시와, 네트워크가 실패했을 때 저장된 모든 책에서 찾기를 맡는다. DB 행(Entity)을 그대로 주고받고,
 * 무엇을 얼마나 남길지(캐시 보관 규칙)는 여기서 정한다.
 */
interface ISearchLocalDataSource {
    suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, books: List<BookEntity>, totalCount: Int, isEnd: Boolean)

    /** 저장된 페이지. 페이지 정보와 책 목록을 한 트랜잭션에서 읽는다. 저장된 페이지가 없으면 null. */
    suspend fun getCachedPage(query: String, sort: SearchSort, page: Int): CachedPage?

    /** 저장된 모든 책에서 검색어로 찾는다. */
    suspend fun findSavedBooks(query: String, sort: SearchSort): List<BookEntity>
}
