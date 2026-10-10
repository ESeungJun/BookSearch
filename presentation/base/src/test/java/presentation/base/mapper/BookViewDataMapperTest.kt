package presentation.base.mapper

import domain.base.data.BookDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 카드 표시 규칙의 경계값. 서버 값이 비거나 특수값(-1)일 때 조용히 틀린 값이 보이지 않는지 본다. */
class BookViewDataMapperTest {

    @Test
    fun `할인가가 있으면 할인가를 보이고 정가는 취소선으로 남긴다`() {
        val data = book(price = 20_000, salePrice = 18_000).toViewData(isFavorite = false)
        assertEquals(18_000, data.price)
        assertEquals(20_000, data.originalPrice)
    }

    @Test
    fun `할인가가 -1(할인 없음)이면 정가만 보인다`() {
        val data = book(price = 20_000, salePrice = -1).toViewData(isFavorite = false)
        assertEquals(20_000, data.price)
        assertNull(data.originalPrice)
    }

    @Test
    fun `할인가와 정가가 같으면 취소선을 두지 않는다`() {
        val data = book(price = 20_000, salePrice = 20_000).toViewData(isFavorite = false)
        assertEquals(20_000, data.price)
        assertNull(data.originalPrice)
    }

    @Test
    fun `정가가 없고 할인가만 있으면 할인가만 보인다`() {
        val data = book(price = null, salePrice = 9_000).toViewData(isFavorite = false)
        assertEquals(9_000, data.price)
        assertNull(data.originalPrice)
    }

    @Test
    fun `가격이 둘 다 없으면 가격은 null 이다(가격 정보 없음)`() {
        val data = book(price = null, salePrice = null).toViewData(isFavorite = false)
        assertNull(data.price)
        assertNull(data.originalPrice)
    }

    @Test
    fun `저자가 두 명을 넘으면 두 명까지 잇고 나머지 수를 센다`() {
        val data = book(authors = listOf("가", "나", "다", "라")).toViewData(isFavorite = false)
        assertEquals("가, 나", data.authors)
        assertEquals(2, data.otherAuthorCount)
    }

    @Test
    fun `저자가 두 명이면 나머지는 0이다`() {
        val data = book(authors = listOf("가", "나")).toViewData(isFavorite = false)
        assertEquals("가, 나", data.authors)
        assertEquals(0, data.otherAuthorCount)
    }

    @Test
    fun `저자가 없거나 빈 이름뿐이면 저자 칸을 비운다`() {
        assertNull(book(authors = null).toViewData(isFavorite = false).authors)
        assertNull(book(authors = emptyList()).toViewData(isFavorite = false).authors)
        assertNull(book(authors = listOf("", " ")).toViewData(isFavorite = false).authors)
    }

    @Test
    fun `빈 문자열은 값이 없는 것으로 본다`() {
        val data = book(title = " ", publisher = "", thumbnailUrl = "").toViewData(isFavorite = true)
        assertNull(data.title)
        assertNull(data.publisher)
        assertNull(data.thumbnailUrl)
        assertEquals(true, data.isFavorite)
    }

    private fun book(
        title: String? = "코틀린",
        authors: List<String>? = listOf("가"),
        publisher: String? = "출판사",
        price: Int? = 10_000,
        salePrice: Int? = null,
        thumbnailUrl: String? = "https://example.com/a.jpg",
    ) = BookDTO(
        key = "key",
        title = title,
        authors = authors,
        publisher = publisher,
        publishedDate = "2024-01-01",
        price = price,
        salePrice = salePrice,
        thumbnailUrl = thumbnailUrl,
        isbn = null,
        description = null,
        url = null,
    )
}
