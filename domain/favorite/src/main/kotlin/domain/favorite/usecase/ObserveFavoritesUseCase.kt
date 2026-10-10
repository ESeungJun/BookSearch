package domain.favorite.usecase

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** 즐겨찾기한 책을 검색어·금액 범위로 골라 본다. 최근에 넣은 것이 앞이다. */
class ObserveFavoritesUseCase @Inject constructor(
    private val repository: IFavoriteRepository,
) {
    operator fun invoke(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> =
        repository.observeFavorites(query, priceRange)
}
