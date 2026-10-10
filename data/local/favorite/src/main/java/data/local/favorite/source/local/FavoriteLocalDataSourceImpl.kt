package data.local.favorite.source.local

import data.base.db.dao.IFavoriteDao
import data.base.db.entity.FavoriteEntity
import data.base.toBook
import data.base.toEntity
import data.base.toLikePattern
import domain.base.data.BookDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteLocalDataSourceImpl @Inject constructor(
    private val favoriteDao: IFavoriteDao,
) : IFavoriteLocalDataSource {

    override fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookDTO>> =
        favoriteDao.observe(
            pattern = query.takeIf { it.isNotEmpty() }?.toLikePattern(),
            minPrice = priceRange?.first,
            maxPrice = priceRange?.last,
        ).map { entities -> entities.map { it.toBook() } }

    override fun observeFavoriteKeys(): Flow<Set<String>> = favoriteDao.observeKeys().map { it.toSet() }

    override suspend fun addFavorite(book: BookDTO) {
        favoriteDao.add(book.toEntity(), FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }
}
