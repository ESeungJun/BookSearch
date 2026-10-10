package presentation.base

import androidx.compose.runtime.Immutable
import domain.base.data.BookDTO

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

fun BookDTO.toViewData(isFavorite: Boolean): BookViewData {
    val names = authors.orEmpty().filter { it.isNotBlank() }
    // 서버는 할인이 없으면 sale_price 를 -1 로 보낸다. 0 이하는 할인가로 보지 않는다
    val discounted = salePrice?.takeIf { it > 0 }
    val listPrice = price?.takeIf { it >= 0 }
    return BookViewData(
        key = key,
        title = title?.takeIf { it.isNotBlank() },
        publishedDate = publishedDate?.takeIf { it.isNotBlank() },
        authors = names.take(SHOWN_AUTHOR_COUNT).joinToString(", ").takeIf { it.isNotEmpty() },
        otherAuthorCount = (names.size - SHOWN_AUTHOR_COUNT).coerceAtLeast(0),
        publisher = publisher?.takeIf { it.isNotBlank() },
        price = discounted ?: listPrice,
        originalPrice = listPrice?.takeIf { discounted != null && it != discounted },
        thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
        isFavorite = isFavorite,
    )
}

private const val SHOWN_AUTHOR_COUNT = 2
