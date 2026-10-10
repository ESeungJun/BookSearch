package presentation.base.mapper

import domain.base.data.BookDTO
import presentation.base.data.BookViewData

/** 카드 한 칸의 표시 값으로 바꾼다. 할인가가 있으면 할인가를 보이고 정가는 취소선으로 함께 보인다. 즐겨찾기 여부는 화면이 즐겨찾기 키에서 계산해 넘긴다. */
fun BookDTO.toViewData(isFavorite: Boolean): BookViewData {
    val names = authorNames()
    val discounted = shownSalePrice()
    val listPrice = shownListPrice()
    return BookViewData(
        key = key,
        title = title.blankToNull(),
        publishedDate = publishedDate.blankToNull(),
        authors = names.take(SHOWN_AUTHOR_COUNT).joinToString(", ").takeIf { it.isNotEmpty() },
        otherAuthorCount = (names.size - SHOWN_AUTHOR_COUNT).coerceAtLeast(0),
        publisher = publisher.blankToNull(),
        price = discounted ?: listPrice,
        // 정가가 할인가보다 클 때만 취소선으로 함께 보인다(정가 0·할인가와 같거나 작은 값은 할인으로 보이지 않는다)
        originalPrice = listPrice?.takeIf { discounted != null && it > discounted },
        thumbnailUrl = thumbnailUrl.blankToNull(),
        isFavorite = isFavorite,
    )
}

private const val SHOWN_AUTHOR_COUNT = 2
