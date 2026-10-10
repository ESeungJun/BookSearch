package com.leeseungjun.booksearch.data

import com.leeseungjun.booksearch.data.api.searchbook.ISearchBookService
import com.leeseungjun.booksearch.data.api.searchbook.SearchBookApi
import com.leeseungjun.booksearch.data.db.book.IBookDao
import com.leeseungjun.booksearch.data.db.book.BookEntity
import com.leeseungjun.booksearch.data.db.favorite.IFavoriteDao
import com.leeseungjun.booksearch.data.db.favorite.FavoriteEntity
import com.leeseungjun.booksearch.data.db.searchcache.ISearchCacheDao
import com.leeseungjun.booksearch.data.db.searchcache.SearchCacheEntity
import com.leeseungjun.booksearch.data.repository.BookRepositoryImpl
import com.leeseungjun.booksearch.domain.model.BookException
import com.leeseungjun.booksearch.domain.model.SearchConditionDTO
import com.leeseungjun.booksearch.domain.model.SearchSort
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** 저장소의 "네트워크 먼저 → 3초 넘거나 실패하면 캐시" 분기를 확인한다. LRU 삭제는 SQL 이라 여기서 다루지 않는다. */
class BookRepositoryImplTest {
    private val api = FakeSearchBookService()
    private val books = FakeBookDao()
    private val cache = FakeSearchCacheDao(books)
    private val repository = BookRepositoryImpl(api, books, FakeFavoriteDao(), cache)

    @Test
    fun `성공하면 결과를 돌려주고 캐시에 저장한다`() = runTest {
        val page = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).getOrThrow()
        assertNull(page.cachedAt)
        assertEquals(2, page.books.size)
        assertEquals(2, cache.rows.size)
    }

    @Test
    fun `3초를 넘기면 저장해 둔 결과를 돌려준다`() = runTest {
        repository.searchBooks("kotlin", SearchSort.ACCURACY, 1)
        api.delayMs = 5_000
        val page = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).getOrThrow()
        assertNotNull(page.cachedAt)
        assertEquals(2, page.books.size)
    }

    @Test
    fun `연결 실패이고 캐시도 없으면 NETWORK 실패다`() = runTest {
        api.error = IOException()
        val error = repository.searchBooks("kotlin", SearchSort.ACCURACY, 1).exceptionOrNull()
        assertEquals(BookException.Reason.NETWORK, (error as BookException).reason)
    }

    @Test
    fun `6페이지부터는 캐시 목록에 남기지 않지만 책 정보는 저장한다`() = runTest {
        repository.searchBooks("kotlin", SearchSort.ACCURACY, 6)
        assertTrue(cache.rows.isEmpty())
        assertNotNull(repository.getBook(books.saved.keys.first()))
    }

    @Test
    fun `마지막으로 저장한 검색을 돌려준다`() = runTest {
        repository.searchBooks("kotlin", SearchSort.LATEST, 1)
        assertEquals(SearchConditionDTO("kotlin", SearchSort.LATEST), repository.getLastSearch())
    }

    private class FakeSearchBookService : ISearchBookService {
        var delayMs = 0L
        var error: Exception? = null
        override suspend fun search(query: String, sort: String, page: Int, size: Int): SearchBookApi {
            delay(delayMs)
            error?.let { throw it }
            val docs = listOf("A", "B").map { SearchBookApi.DocumentApi(title = "$it$page", isbn = "97800000000${it.length}$page") }
            return SearchBookApi(SearchBookApi.MetaApi(totalCount = 40, isEnd = false), docs)
        }
    }

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
