package domain.search.data

import domain.base.data.BookDTO
import domain.base.data.DomainResult

/**
 * 검색 결과 한 페이지.
 * [cachedAt] 이 null 이 아니면 네트워크가 실패해 저장해 둔 결과를 대신 보여 주는 것이다(그 저장 시각, epoch ms).
 * [isLocalMatch] 면 네트워크가 실패했고 같은 검색의 저장 결과도 없어, 기기에 저장된 책에서 검색어로 찾은 결과다
 * (다음 페이지 없음, [totalCount] 는 찾은 수).
 * 둘 중 하나일 때 [failure] 는 원래 실패(Fail·Error)다. 무엇을 알릴지(예: 401 이면 토스트)는 화면이 정한다.
 */
data class SearchPageDTO(
    val books: List<BookDTO>,
    val totalCount: Int,
    val isEnd: Boolean,
    val cachedAt: Long? = null,
    val isLocalMatch: Boolean = false,
    val failure: DomainResult<Nothing>? = null,
)
