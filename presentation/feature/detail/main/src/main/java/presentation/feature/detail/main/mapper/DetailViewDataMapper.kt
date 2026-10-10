package presentation.feature.detail.main.mapper

import domain.base.data.BookDTO
import presentation.base.mapper.authorNames
import presentation.base.mapper.blankToNull
import presentation.base.mapper.shownListPrice
import presentation.base.mapper.shownSalePrice
import presentation.feature.detail.main.data.DetailViewData

/** 상세에 그릴 값으로 바꾼다. 빈 값·가격 기준은 목록 카드와 같다(BookFieldMapper). */
internal fun BookDTO.toDetailViewData(): DetailViewData = DetailViewData(
    title = title.blankToNull(),
    authors = authorNames().joinToString(", ").blankToNull(),
    publisher = publisher.blankToNull(),
    publishedDate = publishedDate.blankToNull(),
    isbn = isbn.blankToNull(),
    price = shownListPrice(),
    salePrice = shownSalePrice(),
    thumbnailUrl = thumbnailUrl.blankToNull(),
    description = description.blankToNull(),
    isDescriptionCut = description?.trimEnd()?.lastOrNull()?.let { it !in SENTENCE_END } ?: false,
    url = url.blankToNull(),
)

// 이 문자로 끝나면 문장이 끝난 것으로 보고 "…" 를 붙이지 않는다
private val SENTENCE_END = setOf('.', '!', '?', '。')
