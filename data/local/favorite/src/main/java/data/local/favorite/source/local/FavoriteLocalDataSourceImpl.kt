package data.local.favorite.source.local

import data.base.db.dao.IFavoriteDao
import data.base.db.entity.BookEntity
import data.base.db.entity.FavoriteEntity
import data.base.toLikePattern
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class FavoriteLocalDataSourceImpl @Inject constructor(
    private val favoriteDao: IFavoriteDao,
) : IFavoriteLocalDataSource {

    override fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookEntity>> =
        favoriteDao.observe(
            pattern = query.takeIf { it.isNotEmpty() }?.toLikePattern(),
            minPrice = priceRange?.first,
            maxPrice = priceRange?.last,
        )

    override fun observeFavoriteKeys(): Flow<List<String>> = favoriteDao.observeKeys()

    override suspend fun addFavorite(book: BookEntity) {
        favoriteDao.add(book, FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }
}
