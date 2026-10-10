package core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import core.database.AbsBookDatabase
import core.database.entity.BookEntity
import core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 조용히 틀릴 수 있는 쿼리 조건만 확인한다: 검색어(제목·저자, 이스케이프)·금액 범위(할인가 우선, 가격 없음 제외)·순서·트랜잭션. */
@RunWith(RobolectricTestRunner::class)
// Robolectric 의 SDK 36 환경은 Java 21 이 필요하다. 과제가 JDK 17 을 지정해 그 아래 최신(35)에서 돌린다
@Config(sdk = [35])
class FavoriteDaoTest {
    private lateinit var db: AbsBookDatabase
    private lateinit var dao: IFavoriteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AbsBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.favoriteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `조건이 없으면 모든 즐겨찾기를 최근에 넣은 순서로 준다`() = runTest {
        add(book("a"), savedAt = 1)
        add(book("b"), savedAt = 3)
        add(book("c"), savedAt = 2)
        assertEquals(listOf("b", "c", "a"), keys(pattern = null))
    }

    @Test
    fun `검색어는 제목과 저자에서 찾고 대소문자를 가리지 않는다`() = runTest {
        add(book("1", title = "Kotlin in Action"))
        add(book("2", title = "자바", authors = listOf("코틀린 저자")))
        add(book("3", title = "스위프트"))
        assertEquals(setOf("1"), keys("%kotlin%").toSet())
        assertEquals(setOf("2"), keys("%코틀린%").toSet())
    }

    @Test
    fun `이스케이프한 퍼센트와 밑줄은 글자 그대로 찾는다`() = runTest {
        add(book("1", title = "100% 코틀린"))
        add(book("2", title = "1000 코틀린"))
        add(book("3", title = "a_b"))
        add(book("4", title = "axb"))
        assertEquals(listOf("1"), keys("%100\\%%"))
        assertEquals(listOf("3"), keys("%a\\_b%"))
    }

    @Test
    fun `금액은 할인가가 있으면 할인가로 보고 양 끝을 포함한다`() = runTest {
        add(book("sale", price = 30_000, salePrice = 9_999))
        add(book("edge", price = 10_000))
        add(book("over", price = 20_000))
        assertEquals(setOf("sale"), keys(null, 0, 9_999).toSet())
        assertEquals(setOf("edge"), keys(null, 10_000, 19_999).toSet())
    }

    @Test
    fun `할인가가 0 이하면 정가로 거른다(카드에 보이는 가격과 같은 기준)`() = runTest {
        add(book("zeroSale", price = 15_000, salePrice = 0))
        assertEquals(listOf("zeroSale"), keys(null, 10_000, 19_999))
    }

    @Test
    fun `금액 범위를 고르면 가격 정보가 없는 책은 빠진다`() = runTest {
        add(book("none"))
        add(book("priced", price = 5_000))
        assertEquals(listOf("priced"), keys(null, 0, Int.MAX_VALUE))
        assertEquals(2, keys(null).size)
    }

    @Test
    fun `add 는 책 정보와 즐겨찾기를 함께 저장하고 delete 는 즐겨찾기만 지운다`() = runTest {
        add(book("a", title = "제목"))
        assertEquals("제목", db.bookDao().get("a")?.title)
        assertEquals(listOf("a"), dao.observeKeys().first())

        dao.delete("a")
        assertEquals(emptyList<String>(), dao.observeKeys().first())
        assertEquals("제목", db.bookDao().get("a")?.title)
    }

    private suspend fun add(book: BookEntity, savedAt: Long = 0) {
        dao.add(book, FavoriteEntity(book.key, savedAt))
    }

    private suspend fun keys(pattern: String?, minPrice: Int? = null, maxPrice: Int? = null): List<String> =
        dao.observe(pattern, minPrice, maxPrice).first().map { it.key }

    private fun book(
        key: String,
        title: String? = key,
        authors: List<String>? = null,
        price: Int? = null,
        salePrice: Int? = null,
    ) = BookEntity(key, title, authors, null, null, price, salePrice, null, null, null, null)
}
