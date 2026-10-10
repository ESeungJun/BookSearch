package data.favorite.repo

import data.favorite.source.local.IFavoriteLocalDataSource
import domain.book.data.BookDTO
import domain.favorite.repo.IFavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 즐겨찾기는 기기에만 저장한다. 원격이 없어 로컬 데이터 소스에 맡긴다. */
class FavoriteRepositoryImpl @Inject constructor(
    private val local: IFavoriteLocalDataSource,
) : IFavoriteRepository {

    override fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookDTO>> =
        local.observeFavorites(query, priceRange)

    override fun observeFavoriteKeys(): Flow<Set<String>> = local.observeFavoriteKeys()

    override suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean) {
        if (isFavorite) local.removeFavorite(book.key) else local.addFavorite(book)
    }
}
