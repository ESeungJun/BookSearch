package data.api.searchbook.source.local

import core.database.dao.IBookDao
import core.database.dao.ISearchCacheDao
import core.database.entity.BookEntity
import core.database.entity.SearchCacheEntity
import domain.base.data.BookDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 캐시 보관 규칙을 확인한다. LRU 삭제는 SQL 이라 여기서 다루지 않는다. */
class SearchLocalDataSourceImplTest {
    private val books = FakeBookDao()
    private val cache = FakeSearchCacheDao(books)
    private val local = SearchLocalDataSourceImpl(books, cache)

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
        assertNotNull(books.saved["A"])
    }

    @Test
    fun `0건 페이지는 캐시에 남기지 않는다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, 1, page())
        assertNull(local.getSearchPage("kotlin", SearchSort.ACCURACY, 1))
    }

    @Test
    fun `저장된 책에서 찾은 결과는 다음 페이지가 없고 기기에서 찾은 것으로 표시한다`() = runTest {
        local.saveSearchPage("kotlin", SearchSort.ACCURACY, 1, page("A", "B"))
        val found = local.findSavedBooks("A", SearchSort.ACCURACY)
        assertEquals(listOf("A"), found?.books?.map { it.key })
        assertTrue(found!!.isLocalMatch && found.isEnd)
    }

    @Test
    fun `저장된 책에서도 못 찾으면 null 이다`() = runTest {
        assertNull(local.findSavedBooks("없는책", SearchSort.ACCURACY))
    }

    private fun page(vararg keys: String) = SearchPageDTO(keys.map(::book), totalCount = keys.size, isEnd = true)

    private fun book(key: String) = BookDTO(key, key, emptyList(), "", "", null, null, null, "", "", "")

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
    }
}
