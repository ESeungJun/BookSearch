package data.favorite.repo

import data.favorite.source.local.IFavoriteLocalDataSource
import domain.book.data.BookDTO
import domain.favorite.repo.IFavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 즐겨찾기는 기기에만 저장한다. 원격이 없어 로컬 데이터 소스에 그대로 맡긴다. */
class FavoriteRepositoryImpl @Inject constructor(
    private val local: IFavoriteLocalDataSource,
) : IFavoriteRepository {

    override fun observeFavorites(): Flow<List<BookDTO>> = local.observeFavorites()

    override suspend fun addFavorite(book: BookDTO) = local.addFavorite(book)

    override suspend fun removeFavorite(key: String) = local.removeFavorite(key)
}
