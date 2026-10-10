package domain.favorite.repo

import domain.book.data.BookDTO
import domain.favorite.data.FavoriteSort
import kotlinx.coroutines.flow.Flow

interface IFavoriteRepository {
    /** 검색어(제목·저자)·금액 범위(실제로 내는 가격)로 고르고 제목순으로 정렬한 즐겨찾기. */
    fun observeFavorites(query: String, sort: FavoriteSort, priceRange: IntRange?): Flow<List<BookDTO>>

    fun observeFavoriteKeys(): Flow<Set<String>>

    /** [isFavorite] 는 지금 상태다. 즐겨찾기면 빼고 아니면 넣는다. */
    suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean)
}
