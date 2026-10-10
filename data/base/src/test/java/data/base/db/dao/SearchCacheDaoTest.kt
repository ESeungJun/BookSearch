package data.base.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import data.base.db.AbsBookDatabase
import data.base.db.entity.BookEntity
import data.base.db.entity.SearchCacheEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 검색 캐시 보관 규칙(새 1페이지는 옛 페이지를 지움, 최근 조합만 남김)과 저장 순서대로 읽기를 실제 SQL 로 확인한다. */
@RunWith(RobolectricTestRunner::class)
// Robolectric 의 SDK 36 환경은 Java 21 이 필요하다. 과제가 JDK 17 을 지정해 그 아래 최신(35)에서 돌린다
@Config(sdk = [35])
class SearchCacheDaoTest {
    private lateinit var db: AbsBookDatabase
    private lateinit var dao: ISearchCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AbsBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.searchCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `저장한 순서대로 책을 읽는다`() = runTest {
        db.bookDao().upsert(listOf(book("A"), book("B"), book("C")))
        dao.savePage("kotlin", SORT, 1, rows("kotlin", 1, "C", "A", "B"), maxCombinations = 20)
        assertEquals(listOf("C", "A", "B"), dao.getBooks("kotlin", SORT, 1).map { it.key })
    }

    @Test
    fun `1페이지를 다시 저장하면 그 조합의 옛 2페이지 이후를 지운다`() = runTest {
        dao.savePage("kotlin", SORT, 1, rows("kotlin", 1, "A"), maxCombinations = 20)
        dao.savePage("kotlin", SORT, 2, rows("kotlin", 2, "B"), maxCombinations = 20)
        dao.savePage("kotlin", SORT, 1, rows("kotlin", 1, "C"), maxCombinations = 20)
        assertEquals(listOf("C"), dao.getPage("kotlin", SORT, 1).map { it.bookKey })
        assertTrue(dao.getPage("kotlin", SORT, 2).isEmpty())
    }

    @Test
    fun `다음 페이지를 다시 저장하면 그 페이지만 바꾼다`() = runTest {
        dao.savePage("kotlin", SORT, 1, rows("kotlin", 1, "A"), maxCombinations = 20)
        dao.savePage("kotlin", SORT, 2, rows("kotlin", 2, "B"), maxCombinations = 20)
        dao.savePage("kotlin", SORT, 2, rows("kotlin", 2, "C"), maxCombinations = 20)
        assertEquals(listOf("A"), dao.getPage("kotlin", SORT, 1).map { it.bookKey })
        assertEquals(listOf("C"), dao.getPage("kotlin", SORT, 2).map { it.bookKey })
    }

    @Test
    fun `가장 최근에 저장한 조합만 정한 개수만큼 남긴다`() = runTest {
        repeat(3) { index -> dao.savePage("q$index", SORT, 1, rows("q$index", 1, "A", savedAt = index.toLong()), maxCombinations = 2) }
        assertTrue(dao.getPage("q0", SORT, 1).isEmpty())
        assertEquals(1, dao.getPage("q1", SORT, 1).size)
        assertEquals(1, dao.getPage("q2", SORT, 1).size)
    }

    @Test
    fun `정렬이 다르면 다른 조합이다`() = runTest {
        dao.savePage("kotlin", "ACCURACY", 1, rows("kotlin", 1, "A", sort = "ACCURACY"), maxCombinations = 20)
        dao.savePage("kotlin", "LATEST", 1, rows("kotlin", 1, "B", sort = "LATEST"), maxCombinations = 20)
        assertEquals(listOf("A"), dao.getPage("kotlin", "ACCURACY", 1).map { it.bookKey })
        assertEquals(listOf("B"), dao.getPage("kotlin", "LATEST", 1).map { it.bookKey })
    }

    private fun rows(query: String, page: Int, vararg keys: String, sort: String = SORT, savedAt: Long = 1L) =
        keys.mapIndexed { position, key -> SearchCacheEntity(query, sort, page, position, key, keys.size, true, savedAt) }

    private fun book(key: String) = BookEntity(key, key, emptyList(), null, null, null, null, null, null, null, null)

    private companion object {
        const val SORT = "ACCURACY"
    }
}
