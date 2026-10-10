package domain.favorite.repo

import domain.book.data.BookDTO
import kotlinx.coroutines.flow.Flow

interface IFavoriteRepository {
    /** 즐겨찾기 목록. 최근에 추가한 것이 앞이다. */
    fun observeFavorites(): Flow<List<BookDTO>>

    suspend fun addFavorite(book: BookDTO)

    suspend fun removeFavorite(key: String)
}
