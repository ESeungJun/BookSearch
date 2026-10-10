package data.data

import domain.book.data.BookDTO

fun SearchBookApi.DocumentApi.toBook(): BookDTO {
    val isbns = isbn.split(' ').filter { it.isNotBlank() }
    val isbn13 = isbns.firstOrNull { it.length == 13 }
    val isbn10 = isbns.firstOrNull { it.length == 10 }
    return BookDTO(
        key = bookKey(isbn13, isbn10),
        title = title,
        authors = authors,
        publisher = publisher,
        publishedDate = datetime.take(10), // "2019-01-12T00:00:00.000+09:00" → "2019-01-12"
        price = price.takeIf { it > 0 },
        salePrice = salePrice.takeIf { it > 0 }, // -1 은 할인 없음
        thumbnailUrl = thumbnail.ifBlank { null },
        isbn = isbn13 ?: isbn10.orEmpty(),
        description = contents,
        url = url,
    )
}

/**
 * 매칭 키 (작성자 결정 D-12·D-13). ISBN 만으로는 다른 책과 겹칠 수 있어 제목·저자를 함께 쓴다.
 * ISBN13 → ISBN10 → (ISBN 없음) 출판사 순으로 정해, 같은 책은 다시 검색해도 같은 키가 나온다.
 */
private fun SearchBookApi.DocumentApi.bookKey(isbn13: String?, isbn10: String?): String {
    val names = authors.joinToString(",")
    return when {
        isbn13 != null -> "$isbn13|$title|$names"
        isbn10 != null -> "$isbn10|$title|$names"
        else -> "$title|$names|$publisher"
    }
}
