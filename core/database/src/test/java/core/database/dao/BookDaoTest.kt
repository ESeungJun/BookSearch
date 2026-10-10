package core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import core.database.AbsBookDatabase
import core.database.entity.BookEntity
import core.database.entity.FavoriteEntity
import core.database.entity.SearchCacheEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 오프라인 검색 쿼리(제목·저자, 이스케이프, 순서, 개수)와 참조 없는 책 정리를 확인한다. */
@RunWith(RobolectricTestRunner::class)
// Robolectric 의 SDK 36 환경은 Java 21 이 필요하다. 과제가 JDK 17 을 지정해 그 아래 최신(35)에서 돌린다
@Config(sdk = [35])
class BookDaoTest {
    private lateinit var db: AbsBookDatabase
    private lateinit var dao: IBookDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AbsBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bookDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `제목이나 저자에 검색어가 들어간 책만 찾는다`() = runTest {
        dao.upsert(
            listOf(
                book("1", title = "코틀린 인 액션"),
                book("2", title = "자바", authors = listOf("코틀린 저자")),
                book("3", title = "스위프트"),
            ),
        )
        assertEquals(setOf("1", "2"), dao.search("%코틀린%", latest = false, limit = 10).map { it.key }.toSet())
    }

    @Test
    fun `이스케이프한 퍼센트와 밑줄은 글자 그대로 찾는다`() = runTest {
        dao.upsert(listOf(book("1", title = "100% 코틀린"), book("2", title = "1000 코틀린"), book("3", title = "a_b"), book("4", title = "axb")))
        assertEquals(listOf("1"), dao.search("%100\\%%", latest = false, limit = 10).map { it.key })
        assertEquals(listOf("3"), dao.search("%a\\_b%", latest = false, limit = 10).map { it.key })
    }

    @Test
    fun `정확도순은 제목에 검색어가 있는 책을 먼저 두고 제목순이다`() = runTest {
        dao.upsert(
            listOf(
                book("author", title = "가", authors = listOf("코틀린")),
                book("b", title = "코틀린 나"),
                book("a", title = "코틀린 가"),
            ),
        )
        assertEquals(listOf("a", "b", "author"), dao.search("%코틀린%", latest = false, limit = 10).map { it.key })
    }

    @Test
    fun `발간일순은 출간일 최신순이고 limit 만큼만 준다`() = runTest {
        dao.upsert(
            listOf(
                book("old", title = "코틀린 1", publishedDate = "2017-01-01"),
                book("new", title = "코틀린 2", publishedDate = "2024-05-01"),
                book("mid", title = "코틀린 3", publishedDate = "2020-03-01"),
            ),
        )
        assertEquals(listOf("new", "mid"), dao.search("%코틀린%", latest = true, limit = 2).map { it.key })
    }

    @Test
    fun `즐겨찾기나 검색 캐시가 가리키지 않는 책만 지운다`() = runTest {
        dao.upsert(listOf(book("favorite"), book("cached"), book("orphan")))
        db.favoriteDao().upsert(FavoriteEntity("favorite", savedAt = 0))
        db.searchCacheDao().insert(
            listOf(SearchCacheEntity("q", "accuracy", 1, 0, "cached", totalCount = 1, isEnd = true, savedAt = 0)),
        )

        dao.deleteUnreferenced()
        assertEquals("favorite", dao.get("favorite")?.key)
        assertEquals("cached", dao.get("cached")?.key)
        assertNull(dao.get("orphan"))
    }

    private fun book(
        key: String,
        title: String? = key,
        authors: List<String>? = null,
        publishedDate: String? = null,
    ) = BookEntity(key, title, authors, null, publishedDate, null, null, null, null, null, null)
}
