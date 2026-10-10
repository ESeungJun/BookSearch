package data.source.local

import domain.book.data.BookDTO
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.flow.Flow

/** 기기에 저장한 책·즐겨찾기·검색 캐시만 맡는다. 무엇을 얼마나 남길지(캐시 보관 규칙)도 여기서 정한다. */
interface IBookLocalDataSource {
    suspend fun saveSearchPage(query: String, sort: SearchSort, page: Int, result: SearchPageDTO)
    suspend fun getSearchPage(query: String, sort: SearchSort, page: Int): SearchPageDTO?
    suspend fun getLastSearch(): SearchConditionDTO?
    suspend fun getBook(key: String): BookDTO?
    fun observeFavorites(): Flow<List<BookDTO>>
    suspend fun addFavorite(book: BookDTO)
    suspend fun removeFavorite(key: String)
}
