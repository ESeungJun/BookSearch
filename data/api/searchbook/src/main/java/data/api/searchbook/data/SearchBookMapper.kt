package data.api.searchbook.data

import domain.base.data.BookDTO
import domain.search.data.SearchPageDTO

/** 목록·총 개수·끝 여부가 없으면 페이지를 만들 수 없어 null 이다(빈 결과로 보지 않는다). */
fun SearchBookApi.toPageOrNull(): SearchPageDTO? {
    val totalCount = meta?.totalCount ?: return null
    val isEnd = meta.isEnd ?: return null
    val documents = documents ?: return null
    return SearchPageDTO(documents.map { it.toBook() }, totalCount, isEnd)
}

/** 서버 값을 그대로 옮긴다. 바꾸는 것은 ISBN 나누기, 날짜만 남기기, 할인가 -1(할인 없음 표시)뿐이다. */
fun SearchBookApi.DocumentApi.toBook(): BookDTO {
    val isbns = isbn?.split(' ')?.filter { it.isNotBlank() }.orEmpty()
    val isbn13 = isbns.firstOrNull { it.length == 13 }
    val isbn10 = isbns.firstOrNull { it.length == 10 }
    return BookDTO(
        key = bookKey(isbn13, isbn10),
        title = title,
        authors = authors,
        publisher = publisher,
        publishedDate = datetime?.take(10), // "2019-01-12T00:00:00.000+09:00" → "2019-01-12"
        price = price,
        salePrice = salePrice?.takeUnless { it == NO_SALE_PRICE },
        thumbnailUrl = thumbnail,
        isbn = isbn13 ?: isbn10,
        description = contents,
        url = url,
    )
}

/**
 * 매칭 키. ISBN 만으로는 다른 책과 겹칠 수 있어 제목·저자를 함께 쓴다.
 * ISBN13 → ISBN10 → (ISBN 없음) 출판사 순으로 정해, 같은 책은 다시 검색해도 같은 키가 나온다.
 */
private fun SearchBookApi.DocumentApi.bookKey(isbn13: String?, isbn10: String?): String {
    // 키는 식별용이라 값이 없으면 빈 문자열로 이어 붙인다. 화면에 보이는 값은 null 그대로 둔다
    val names = authors.orEmpty().joinToString(",")
    val title = title.orEmpty()
    return when {
        isbn13 != null -> "$isbn13|$title|$names"
        isbn10 != null -> "$isbn10|$title|$names"
        else -> "$title|$names|${publisher.orEmpty()}"
    }
}

// 서버가 "할인 없음"을 -1 로 보낸다. 가격이 아니라 표시이므로 null 로 바꾼다
private const val NO_SALE_PRICE = -1
