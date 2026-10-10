package presentation.feature.detail.main.mapper

import domain.base.data.BookDTO
import presentation.feature.detail.main.data.DetailViewData

/** 상세에 그릴 값으로 바꾼다. 빈 문자열은 null(그 줄을 생략)이고, 0 이하 할인가는 할인으로 보지 않으며, 음수 정가는 가격 정보 없음이다. */
fun BookDTO.toDetailViewData(): DetailViewData = DetailViewData(
    title = title?.takeIf { it.isNotBlank() },
    authors = authors.orEmpty().filter { it.isNotBlank() }.joinToString(", ").takeIf { it.isNotEmpty() },
    publisher = publisher?.takeIf { it.isNotBlank() },
    publishedDate = publishedDate?.takeIf { it.isNotBlank() },
    isbn = isbn?.takeIf { it.isNotBlank() },
    price = price?.takeIf { it >= 0 },
    salePrice = salePrice?.takeIf { it > 0 },
    thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
    description = description?.takeIf { it.isNotBlank() },
    isDescriptionCut = description?.trimEnd()?.lastOrNull()?.let { it !in SENTENCE_END } ?: false,
    url = url?.takeIf { it.isNotBlank() },
)

// 이 문자로 끝나면 문장이 끝난 것으로 보고 "…" 를 붙이지 않는다
private val SENTENCE_END = setOf('.', '!', '?', '。')
