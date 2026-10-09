package com.leeseungjun.booksearch.domain.model

/**
 * 책 한 권. [key] 는 같은 책을 다시 검색해도 같은 값이 나오는 매칭 키다(docs/api.md 「매칭 키」).
 * 즐겨찾기 상태를 맞추고 목록 key 로도 쓴다.
 */
data class Book(
    val key: String,
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val publishedDate: String, // yyyy-MM-dd, 없으면 빈 문자열
    val price: Int?, // 정가. 0 이하는 정보 없음으로 보고 null
    val salePrice: Int?, // 할인가. API 의 -1(할인 없음)은 null
    val thumbnailUrl: String?,
    val isbn: String,
    val description: String,
    val url: String,
) {
    /** 실제로 내는 가격. 즐겨찾기 금액 필터의 기준이다. */
    val displayPrice: Int? get() = salePrice ?: price
}
