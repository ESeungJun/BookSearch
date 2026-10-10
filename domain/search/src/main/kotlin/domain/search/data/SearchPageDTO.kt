package domain.search.data

import domain.base.data.BookDTO
import domain.base.data.DomainResult

/**
 * 검색 결과 한 페이지.
 * [cachedAt] 이 null 이 아니면 네트워크가 실패해 저장해 둔 결과를 대신 보여 주는 것이다(그 저장 시각, epoch ms).
 * 그때 [failure] 는 원래 실패(Fail·Error)다. 무엇을 알릴지(예: 401 이면 토스트)는 화면이 정한다.
 */
data class SearchPageDTO(
    val books: List<BookDTO>,
    val totalCount: Int,
    val isEnd: Boolean,
    val cachedAt: Long? = null,
    val failure: DomainResult<Nothing>? = null,
)
