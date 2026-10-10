package data.api.searchbook.source.local

import data.base.db.dao.IBookDao
import data.base.db.dao.ISearchCacheDao
import data.base.db.entity.BookEntity
import data.base.db.entity.SearchCacheEntity
import data.base.toLikePattern
import domain.search.data.SearchSort
import javax.inject.Inject

/** Room 의 suspend DAO 는 자체 스레드에서 실행되므로 여기서 디스패처를 바꾸지 않는다. */
class SearchLocalDataSourceImpl @Inject constructor(
    private val bookDao: IBookDao,
    private val searchCacheDao: ISearchCacheDao,
) : ISearchLocalDataSource {

    /**
     * 책 정보는 페이지와 상관없이 저장한다 — 상세 화면이 키로 찾을 수 있어야 한다.
     * 캐시 목록은 조합당 5페이지까지만 남긴다. 결과가 0건인 페이지는 남길 것이 없어 저장하지 않는다.
     */
    override suspend fun saveSearchPage(
        query: String,
        sort: SearchSort,
        page: Int,
        books: List<BookEntity>,
        totalCount: Int,
        isEnd: Boolean,
    ) {
        bookDao.upsert(books)
        if (page > MAX_CACHED_PAGES || books.isEmpty()) return
        val now = System.currentTimeMillis()
        val rows = books.mapIndexed { position, book ->
            SearchCacheEntity(query, sort.name, page, position, book.key, totalCount, isEnd, now)
        }
        searchCacheDao.savePage(query, sort.name, page, rows, MAX_COMBINATIONS)
        if (page == 1) bookDao.deleteUnreferenced()
    }

    override suspend fun getCachedRows(query: String, sort: SearchSort, page: Int): List<SearchCacheEntity> =
        searchCacheDao.getPage(query, sort.name, page)

    override suspend fun getCachedBooks(query: String, sort: SearchSort, page: Int): List<BookEntity> =
        searchCacheDao.getBooks(query, sort.name, page)

    override suspend fun findSavedBooks(query: String, sort: SearchSort): List<BookEntity> =
        bookDao.search(query.toLikePattern(), latest = sort == SearchSort.LATEST, limit = MAX_LOCAL_RESULTS)

    companion object {
        private const val MAX_CACHED_PAGES = 5
        private const val MAX_COMBINATIONS = 20
        private const val MAX_LOCAL_RESULTS = 100 // 저장된 책은 많아야 약 2,000권이고 다음 페이지가 없어 한 번에 보여 줄 만큼만
    }
}
