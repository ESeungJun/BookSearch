package presentation.detail.main.mapper

import domain.base.data.BookDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 상세 표시 값의 경계. 서버 값이 비거나 특수값일 때 조용히 틀린 줄이 보이지 않는지 본다. */
class DetailViewDataMapperTest {

    @Test
    fun `빈 문자열은 값 없음이라 그 줄이 생략된다`() {
        val data = book().copy(title = " ", isbn = "", url = "").toDetailViewData()
        assertNull(data.title)
        assertNull(data.isbn)
        assertNull(data.url)
    }

    @Test
    fun `저자는 빈 이름을 빼고 모두 잇는다`() {
        assertEquals("가, 나, 다", book().copy(authors = listOf("가", "", "나", "다")).toDetailViewData().authors)
        assertNull(book().copy(authors = listOf(" ")).toDetailViewData().authors)
    }

    @Test
    fun `0 이하 할인가는 할인이 아니고 음수 정가는 가격 정보 없음이다`() {
        val data = book().copy(price = -1, salePrice = 0).toDetailViewData()
        assertNull(data.price)
        assertNull(data.salePrice)
    }

    @Test
    fun `정가 0 원과 할인가는 그대로 보인다`() {
        val data = book().copy(price = 0, salePrice = 9_000).toDetailViewData()
        assertEquals(0, data.price)
        assertEquals(9_000, data.salePrice)
    }

    @Test
    fun `소개가 문장 중간에서 끊겼을 때만 말줄임 표시를 한다`() {
        assertEquals(true, book().copy(description = "코틀린은 자바와").toDetailViewData().isDescriptionCut)
        assertEquals(false, book().copy(description = "코틀린을 다룬다.").toDetailViewData().isDescriptionCut)
    }

    private fun book() = BookDTO(
        key = "k", title = "제목", authors = null, publisher = null, publishedDate = null,
        price = null, salePrice = null, thumbnailUrl = null, isbn = null, description = null, url = null,
    )
}
