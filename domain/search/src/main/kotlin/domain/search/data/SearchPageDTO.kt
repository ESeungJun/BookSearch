package domain.search.data

import domain.base.data.BookDTO

/**
 * 검색 결과 한 페이지.
 * [cachedAt] 이 null 이 아니면 네트워크가 실패해 저장해 둔 결과를 대신 보여 주는 것이다(그 저장 시각, epoch ms).
 */
data class SearchPageDTO(
    val books: List<BookDTO>,
    val totalCount: Int,
    val isEnd: Boolean,
    val cachedAt: Long? = null,
)
