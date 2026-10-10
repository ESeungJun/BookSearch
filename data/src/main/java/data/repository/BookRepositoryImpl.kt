package data.repository

import data.api.searchbook.ISearchBookService
import data.api.searchbook.SearchBookApi
import data.api.searchbook.toBook
import data.db.book.IBookDao
import data.db.book.toBook
import data.db.book.toEntity
import data.db.favorite.IFavoriteDao
import data.db.favorite.FavoriteEntity
import data.db.searchcache.ISearchCacheDao
import data.db.searchcache.SearchCacheEntity
import domain.repo.IBookRepository
import domain.data.BookDTO
import domain.data.SearchConditionDTO
import domain.data.BookException
import domain.data.BookException.Reason
import domain.data.SearchPageDTO
import domain.data.SearchSort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * 검색은 **네트워크를 먼저** 시도하고, 3초를 넘기거나 실패하면 저장해 둔 결과를 대신 돌려준다(D-15·D-23).
 * 연결 상태를 미리 확인하지 않는다 — 연결이 없으면 요청이 곧바로 실패해 같은 경로로 캐시를 보게 되고,
 * 연결은 있지만 느린 경우(지하철)는 연결 상태로는 알 수 없어 어차피 시간 제한이 필요하다.
 *
 * Room 의 suspend DAO 는 자체 스레드에서 실행되므로 여기서 디스패처를 바꾸지 않는다.
 */
class BookRepositoryImpl @Inject constructor(
    private val api: ISearchBookService,
    private val bookDao: IBookDao,
    private val favoriteDao: IFavoriteDao,
    private val searchCacheDao: ISearchCacheDao,
) : IBookRepository {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO> {
        val response = try {
            withTimeoutOrNull(TIMEOUT_MS) { api.search(query, sort.apiValue, page, PAGE_SIZE) }
                ?: return fromCache(query, sort, page, BookException(Reason.NETWORK))
        } catch (e: CancellationException) {
            // 화면을 떠나거나 새 검색으로 이전 요청이 취소된 것이다. 실패가 아니므로 캐시로 넘기지 않고 그대로 전한다
            throw e
        } catch (e: Exception) {
            return fromCache(query, sort, page, e.toBookException())
        }
        val books = response.documents.map { it.toBook() }
        save(query, sort, page, response.meta, books)
        return Result.success(SearchPageDTO(books, response.meta.totalCount, response.meta.isEnd))
    }

    override suspend fun getBook(key: String): BookDTO? = bookDao.get(key)?.toBook()

    override suspend fun getLastSearch(): SearchConditionDTO? =
        searchCacheDao.getLatest()?.let { SearchConditionDTO(it.query, SearchSort.valueOf(it.sort)) }

    override fun observeFavorites(): Flow<List<BookDTO>> =
        favoriteDao.observeAll().map { entities -> entities.map { it.toBook() } }

    override suspend fun addFavorite(book: BookDTO) {
        bookDao.upsert(listOf(book.toEntity()))
        favoriteDao.upsert(FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }

    /**
     * 책 정보는 페이지와 상관없이 저장한다 — 상세 화면이 키로 찾을 수 있어야 한다(D-08).
     * 캐시 목록은 조합당 5페이지까지만 남긴다. 결과가 0건인 페이지는 남길 것이 없어 저장하지 않는다.
     */
    private suspend fun save(query: String, sort: SearchSort, page: Int, meta: SearchBookApi.MetaApi, books: List<BookDTO>) {
        bookDao.upsert(books.map { it.toEntity() })
        if (page > MAX_CACHED_PAGES || books.isEmpty()) return
        val now = System.currentTimeMillis()
        val rows = books.mapIndexed { position, book ->
            SearchCacheEntity(query, sort.name, page, position, book.key, meta.totalCount, meta.isEnd, now)
        }
        searchCacheDao.savePage(query, sort.name, page, rows, MAX_COMBINATIONS)
        if (page == 1) bookDao.deleteUnreferenced()
    }

    private suspend fun fromCache(query: String, sort: SearchSort, page: Int, error: BookException): Result<SearchPageDTO> {
        val rows = searchCacheDao.getPage(query, sort.name, page)
        if (rows.isEmpty()) return Result.failure(error)
        val books = searchCacheDao.getBooks(query, sort.name, page).map { it.toBook() }
        val first = rows.first()
        return Result.success(SearchPageDTO(books, first.totalCount, first.isEnd, cachedAt = first.savedAt))
    }

    private fun Exception.toBookException(): BookException {
        val reason = when {
            this is IOException -> Reason.NETWORK
            this is HttpException && (code() == 401 || code() == 403) -> Reason.AUTH
            // 5xx·429·응답 형식 오류. 자동 재시도는 하지 않고 화면의 "다시 시도"에 맡긴다(D-47)
            else -> Reason.SERVER
        }
        // 401 응답 본문에 키 일부가 들어 있어 원인 예외를 그대로 붙이지 않는다(로그로 새지 않게)
        return BookException(reason)
    }

    private val SearchSort.apiValue: String
        get() = when (this) {
            SearchSort.ACCURACY -> "accuracy"
            SearchSort.LATEST -> "latest"
        }

    companion object {
        private const val TIMEOUT_MS = 3_000L
        private const val PAGE_SIZE = 20
        private const val MAX_CACHED_PAGES = 5
        private const val MAX_COMBINATIONS = 20
    }
}
