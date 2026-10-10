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

    override fun observeFavorites(query: String, priceRange: IntRange?): Flow<List<BookDTO>> =
        favoriteDao.observe(
            pattern = query.takeIf { it.isNotEmpty() }?.let { "%${it.escapeLike()}%" },
            minPrice = priceRange?.first,
            maxPrice = priceRange?.last,
        ).map { entities -> entities.map { it.toBook() } }

    override fun observeFavoriteKeys(): Flow<Set<String>> = favoriteDao.observeKeys().map { it.toSet() }

    override suspend fun addFavorite(book: BookDTO) {
        bookDao.upsert(listOf(book.toEntity()))
        favoriteDao.upsert(FavoriteEntity(book.key, System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(key: String) {
        favoriteDao.delete(key)
    }

    // 사용자가 입력한 % · _ 가 LIKE 의 와일드카드로 동작하지 않게 한다
    private fun String.escapeLike(): String = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
