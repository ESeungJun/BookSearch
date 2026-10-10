package data.source.local

import data.db.dao.IBookDao
import data.db.dao.IFavoriteDao
import data.db.dao.ISearchCacheDao
import data.db.entity.FavoriteEntity
import data.db.entity.SearchCacheEntity
import data.db.entity.toBook
import data.db.entity.toEntity
import domain.data.BookDTO
import domain.data.SearchConditionDTO
import domain.data.SearchPageDTO
import domain.data.SearchSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Room 의 suspend DAO 는 자체 스레드에서 실행되므로 여기서 디스패처를 바꾸지 않는다. */
class BookLocalDataSourceImpl @Inject constructor(
    private val bookDao: IBookDao,
    private val favoriteDao: IFavoriteDao,
    private val searchCacheDao: ISearchCacheDao,
) : IBookLocalDataSource {

    /**
     * 책 정보는 페이지와 상관없이 저장한다 — 상세 화면이 키로 찾을 수 있어야 한다(D-08).
     * 캐시 목록은 조합당 5페이지까지만 남긴다. 결과가 0건인 페이지는 남길 것이 없어 저장하지 않는다.
     */
    override suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO) {
        bookDao.upsert(result.books.map { it.toEntity() })
        if (page > MAX_CACHED_PAGES || result.books.isEmpty()) return
        val now = System.currentTimeMillis()
        val rows = result.books.mapIndexed { position, book ->
            SearchCacheEntity(query, sort.name, page, position, book.key, result.totalCount, result.isEnd, now)
        }
        searchCacheDao.savePage(query, sort.name, page, rows, MAX_COMBINATIONS)
        if (page == 1) bookDao.deleteUnreferenced()
    }

    override suspend fun getSearchPage(query: String, sort: SearchSort, page: Int): SearchPageDTO? {
        val first = searchCacheDao.getPage(query, sort.name, page).firstOrNull() ?: return null
        val books = searchCacheDao.getBooks(query, sort.name, page).map { it.toBook() }
        return SearchPageDTO(books, first.totalCount, first.isEnd, cachedAt = first.savedAt)
    }

    override suspend fun getLastSearch(): SearchConditionDTO? =
        searchCacheDao.getLatest()?.let { SearchConditionDTO(it.query, SearchSort.valueOf(it.sort)) }

    override suspend fun getBook(key: String): BookDTO? = bookDao.get(key)?.toBook()

    override fun observeFavorites(): Flow<List<BookDTO>> =
        favoriteDao.observeAll().map { entities -> entities.map { it.toBook() } }

    override suspend fun addFavorite(book: BookDTO) {
        bookDao.upsert(listOf(book.toEntity()))
        favoriteDao.upsert(FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }

    companion object {
        private const val MAX_CACHED_PAGES = 5
        private const val MAX_COMBINATIONS = 20
    }
}
