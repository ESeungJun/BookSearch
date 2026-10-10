package presentation.base.data

import androidx.compose.runtime.Immutable

/**
 * 책 카드 한 칸에 그릴 값. 서버가 주지 않은 값(null·빈 문자열)은 null 로 두고, 대체 문구는 카드가 정한다.
 * 즐겨찾기 여부는 검색 결과에 저장하지 않고 화면이 즐겨찾기 키에서 계산해 넣는다.
 */
@Immutable
data class BookViewData(
    val key: String,
    val title: String?,
    val publishedDate: String?,
    val authors: String?, // 앞의 두 명까지 ", " 로 잇는다
    val otherAuthorCount: Int, // 나머지 저자 수("외 N명")
    val publisher: String?,
    val price: Int?, // 실제로 내는 가격(할인가가 있으면 할인가)
    val originalPrice: Int?, // 할인 중일 때만 정가(취소선)
    val thumbnailUrl: String?,
    val isFavorite: Boolean,
)
