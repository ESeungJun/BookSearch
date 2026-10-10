package presentation.base.mapper

import domain.base.data.BookDTO
import presentation.base.data.BookViewData

/** 카드 한 칸의 표시 값으로 바꾼다. 즐겨찾기 여부는 화면이 즐겨찾기 키에서 계산해 넘긴다. */
fun BookDTO.toViewData(isFavorite: Boolean): BookViewData {
    val names = authors.orEmpty().filter { it.isNotBlank() }
    // 0 이하는 할인가로 보이지 않는다(정가만 보인다). 음수 정가는 가격 정보 없음으로 본다
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
