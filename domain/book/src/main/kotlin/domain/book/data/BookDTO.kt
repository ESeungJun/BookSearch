package domain.book.data

/**
 * 책 한 권. [key] 는 같은 책을 다시 검색해도 같은 값이 나오는 매칭 키다(docs/api.md 「매칭 키」).
 * 즐겨찾기 상태를 맞추고 목록 key 로도 쓴다.
 */
data class BookDTO(
    val key: String, // 항상 있다
    // 아래는 서버가 보낸 그대로다. null 은 "서버가 값을 주지 않음"이고, 어떻게 보여 줄지는 화면이 정한다(D-60)
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?, // yyyy-MM-dd
    val price: Int?, // 정가
    val salePrice: Int?, // 할인가. 할인이 없으면 null
    val thumbnailUrl: String?,
    val isbn: String?, // ISBN13, 없으면 ISBN10
    val description: String?,
    val url: String?,
)
