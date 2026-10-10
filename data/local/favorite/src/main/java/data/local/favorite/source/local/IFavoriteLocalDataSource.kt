package data.local.favorite.source.local

import data.base.db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

/** 즐겨찾기만 맡는다. 책 정보는 book 테이블에 두고 즐겨찾기는 키와 저장 시각만 둔다. */
interface IFavoriteLocalDataSource {
    fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookEntity>>
    fun observeFavoriteKeys(): Flow<List<String>>
    suspend fun addFavorite(book: BookEntity)
    suspend fun removeFavorite(key: String)
}
