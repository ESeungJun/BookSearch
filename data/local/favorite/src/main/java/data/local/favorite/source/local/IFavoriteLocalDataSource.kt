package data.local.favorite.source.local

import domain.base.data.BookDTO
import kotlinx.coroutines.flow.Flow

/** 즐겨찾기만 맡는다. 책 정보는 book 테이블에 두고 즐겨찾기는 키와 저장 시각만 둔다. */
interface IFavoriteLocalDataSource {
    fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookDTO>>
    fun observeFavoriteKeys(): Flow<Set<String>>
    suspend fun addFavorite(book: BookDTO)
    suspend fun removeFavorite(key: String)
}
