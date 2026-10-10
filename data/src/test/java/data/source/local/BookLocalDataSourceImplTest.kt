package data.source.local

import data.db.dao.IBookDao
import data.db.dao.IFavoriteDao
import data.db.dao.ISearchCacheDao
import data.db.entity.BookEntity
import data.db.entity.FavoriteEntity
import data.db.entity.SearchCacheEntity
import domain.data.BookDTO
import domain.data.SearchConditionDTO
import domain.data.SearchPageDTO
import domain.data.SearchSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 캐시 보관 규칙을 확인한다. LRU 삭제는 SQL 이라 여기서 다루지 않는다. */
class BookLocalDataSourceImplTest {
    private val books = FakeBookDao()
    private val cache = FakeSearchCacheDao(books)
    private val local = BookLocalDataSourceImpl(books, FakeFavoriteDao(), cache)

    @Test
    fun `저장한 페이지를 저장 시각과 함께 돌려준다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, 1, page("A", "B"))
        val cached = local.getSearchPage("kotlin", SearchSort.ACCURACY, 1)
        assertEquals(listOf("A", "B"), cached?.books?.map { it.key })
        assertNotNull(cached?.cachedAt)
    }

    @Test
    fun `6페이지부터는 캐시 목록에 남기지 않지만 책 정보는 저장한다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, 6, page("A"))
        assertTrue(cache.rows.isEmpty())
        assertNotNull(local.getBook("A"))
    }

    @Test
    fun `0건 페이지는 캐시에 남기지 않는다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, 1, page())
        assertNull(local.getSearchPage("kotlin", SearchSort.ACCURACY, 1))
    }

    @Test
    fun `마지막으로 저장한 검색을 돌려준다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.LATEST, 1, page("A"))
        assertEquals(SearchConditionDTO("kotlin", SearchSort.LATEST), local.getLastSearch())
    }

    private fun page(vararg keys: String) = SearchPageDTO(keys.map(::book), totalCount = keys.size, isEnd = true)

    private fun book(key: String) = BookDTO(key, key, emptyList(), "", "", null, null, null, "", "", "")

    private class FakeBookDao : IBookDao {
        val saved = linkedMapOf<String, BookEntity>()
        override suspend fun upsert(books: List<BookEntity>) = books.forEach { saved[it.key] = it }
        override suspend fun get(key: String) = saved[key]
        override suspend fun deleteUnreferenced() = Unit
    }

    private class FakeFavoriteDao : IFavoriteDao {
        override fun observeAll(): Flow<List<BookEntity>> = emptyFlow()
        override suspend fun upsert(favorite: FavoriteEntity) = Unit
        override suspend fun delete(key: String) = Unit
    }

    private class FakeSearchCacheDao(private val books: FakeBookDao) : ISearchCacheDao {
        val rows = mutableListOf<SearchCacheEntity>()
        override suspend fun getLatest() = rows.maxByOrNull { it.savedAt }
        override suspend fun getPage(query: String, sort: String, page: Int) =
            rows.filter { it.query == query && it.sort == sort && it.page == page }.sortedBy { it.position }
        override suspend fun getBooks(query: String, sort: String, page: Int) =
            getPage(query, sort, page).mapNotNull { books.saved[it.bookKey] }
        override suspend fun insert(rows: List<SearchCacheEntity>) { this.rows += rows }
        override suspend fun deleteCombination(query: String, sort: String) { rows.removeAll { it.query == query && it.sort == sort } }
        override suspend fun deletePage(query: String, sort: String, page: Int) {
            rows.removeAll { it.query == query && it.sort == sort && it.page == page }
        }
        override suspend fun deleteOldCombinations(max: Int) = Unit
    }
}
