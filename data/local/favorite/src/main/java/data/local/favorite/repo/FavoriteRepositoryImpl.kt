package data.local.favorite.repo

import data.base.safeDbFlow
import data.base.safeDbCall
import data.local.favorite.source.local.IFavoriteLocalDataSource
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 즐겨찾기는 기기에만 저장한다. 원격이 없어 로컬 데이터 소스에 맡긴다. */
class FavoriteRepositoryImpl @Inject constructor(
    private val local: IFavoriteLocalDataSource,
) : IFavoriteRepository {

    override fun observeFavorites(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> =
        local.observeFavorites(query, priceRange).safeDbFlow()

    override fun observeFavoriteKeys(): Flow<DomainResult<Set<String>>> = local.observeFavoriteKeys().safeDbFlow()

    override suspend fun toggleFavorite(book: BookDTO, isFavorite: Boolean): DomainResult<Unit> =
        safeDbCall { if (isFavorite) local.removeFavorite(book.key) else local.addFavorite(book) }
}
