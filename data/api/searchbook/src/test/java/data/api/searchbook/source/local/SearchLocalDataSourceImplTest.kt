package data.api.searchbook.source.local

import data.base.db.dao.IBookDao
import data.base.db.dao.ISearchCacheDao
import data.base.db.entity.BookEntity
import data.base.db.entity.SearchCacheEntity
import domain.search.data.SearchSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 캐시 보관 규칙을 확인한다. LRU 삭제는 SQL 이라 여기서 다루지 않는다. */
class SearchLocalDataSourceImplTest {
    private val books = FakeBookDao()
    private val cache = FakeSearchCacheDao(books)
    private val local = SearchLocalDataSourceImpl(books, cache)

    @Test
    fun `저장한 페이지의 책을 순서대로, 캐시 행에 저장 시각을 남긴다`() = runTest {
        save(1, "A", "B")
        assertEquals(listOf("A", "B"), local.getCachedBooks("kotlin", SearchSort.ACCURACY, 1).map { it.key })
        assertTrue(local.getCachedRows("kotlin", SearchSort.ACCURACY, 1).all { it.savedAt > 0 })
    }

    @Test
    fun `6페이지부터는 캐시 목록에 남기지 않지만 책 정보는 저장한다`() = runTest {
        save(6, "A")
        assertTrue(cache.rows.isEmpty())
        assertNotNull(books.saved["A"])
    }

    @Test
    fun `0건 페이지는 캐시에 남기지 않는다`() = runTest {
        save(1)
        assertTrue(local.getCachedRows("kotlin", SearchSort.ACCURACY, 1).isEmpty())
    }

    @Test
    fun `저장된 모든 책에서 검색어로 찾는다`() = runTest {
        save(1, "A", "B")
        assertEquals(listOf("A"), local.findSavedBooks("A", SearchSort.ACCURACY).map { it.key })
    }

    private suspend fun save(page: Int, vararg keys: String) =
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, page, keys.map(::book), totalCount = keys.size, isEnd = true)

    private fun book(key: String) = BookEntity(key, key, emptyList(), "", "", null, null, null, "", "", "")

    private class FakeBookDao : IBookDao {
        val saved = linkedMapOf<String, BookEntity>()
        override suspend fun upsert(books: List<BookEntity>) = books.forEach { saved[it.key] = it }
        override suspend fun get(key: String) = saved[key]
        override suspend fun deleteUnreferenced() = Unit
        override suspend fun search(pattern: String, latest: Boolean, limit: Int) =
            saved.values.filter { it.title.orEmpty().contains(pattern.trim('%')) }.take(limit)
    }


    private class FakeSearchCacheDao(private val books: FakeBookDao) : ISearchCacheDao {
        val rows = mutableListOf<SearchCacheEntity>()
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
        override suspend fun inTransaction(block: suspend () -> Unit) = block()
    }
}
