package data.favorite.source.local

import data.book.db.dao.IBookDao
import data.book.db.dao.IFavoriteDao
import data.book.db.entity.FavoriteEntity
import data.book.db.entity.toBook
import data.book.db.entity.toEntity
import domain.book.data.BookDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteLocalDataSourceImpl @Inject constructor(
    private val bookDao: IBookDao,
    private val favoriteDao: IFavoriteDao,
) : IFavoriteLocalDataSource {

    override fun observeFavorites(): Flow<List<BookDTO>> =
        favoriteDao.observeAll().map { entities -> entities.map { it.toBook() } }

    override suspend fun addFavorite(book: BookDTO) {
        bookDao.upsert(listOf(book.toEntity()))
        favoriteDao.upsert(FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }
}
