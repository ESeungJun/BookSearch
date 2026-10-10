package presentation.base.mapper

import domain.base.data.BookDTO

// 목록 카드와 상세가 같은 기준으로 값을 보이도록 표시 규칙을 한곳에 둔다

/** 빈 문자열·공백뿐인 값은 없는 것으로 본다(그 줄을 생략한다). */
fun String?.blankToNull(): String? = this?.takeIf { it.isNotBlank() }

/** 저자 이름. 빈 이름은 뺀다. */
fun BookDTO.authorNames(): List<String> = authors.orEmpty().filter { it.isNotBlank() }

/** 보일 할인가. 0 이하는 할인으로 보지 않는다(서버가 할인 없음을 -1 로 보낸다). */
fun BookDTO.shownSalePrice(): Int? = salePrice?.takeIf { it > 0 }

/** 보일 정가. 음수는 가격 정보 없음이다. */
fun BookDTO.shownListPrice(): Int? = price?.takeIf { it >= 0 }
