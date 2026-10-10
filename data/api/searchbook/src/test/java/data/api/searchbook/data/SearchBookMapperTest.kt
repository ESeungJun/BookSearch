package data.api.searchbook.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchBookMapperTest {
    private val doc = SearchBookApi.DocumentApi(
        title = "코틀린", contents = null, url = null, isbn = null, datetime = null,
        authors = listOf("가", "나"), publisher = "출판", price = 20000, salePrice = null, thumbnail = null,
    )

    @Test
    fun `두 ISBN 이 모두 있으면 ISBN13 으로 키를 만든다`() {
        val book = doc.copy(isbn = "8966262422 9788966262427").toBook()
        assertEquals("9788966262427|코틀린|가,나", book.key)
        assertEquals("9788966262427", book.isbn)
    }

    @Test
    fun `ISBN10 만 있으면 ISBN10 으로 키를 만든다`() {
        assertEquals("8966262422|코틀린|가,나", doc.copy(isbn = "8966262422").toBook().key)
    }

    @Test
    fun `ISBN 이 없으면 제목 저자 출판사로 키를 만든다`() {
        assertEquals("코틀린|가,나|출판", doc.toBook().key)
    }

    @Test
    fun `할인가 -1 은 할인 없음이라 null 이다`() {
        val book = doc.copy(salePrice = -1).toBook()
        assertNull(book.salePrice)
    }

    @Test
    fun `서버가 보내지 않은 값은 빈 값으로 채우지 않고 null 그대로 둔다`() {
        val book = doc.copy(title = null, authors = null, price = null).toBook()
        assertNull(book.title)
        assertNull(book.authors)
        assertNull(book.price)
    }

    @Test
    fun `출간일은 날짜 부분만 남긴다`() {
        assertEquals("2019-01-12", doc.copy(datetime = "2019-01-12T00:00:00.000+09:00").toBook().publishedDate)
    }
}
