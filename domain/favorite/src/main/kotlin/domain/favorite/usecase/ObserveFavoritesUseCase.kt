package domain.favorite.usecase

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** 즐겨찾기한 책을 검색어·금액 범위로 골라 본다. 고르는 규칙은 :data:favorite, 보여 줄 순서는 화면이 정한다. */
class ObserveFavoritesUseCase @Inject constructor(
    private val repository: IFavoriteRepository,
) {
    operator fun invoke(query: String, priceRange: IntRange?): Flow<DomainResult<List<BookDTO>>> =
        repository.observeFavorites(query, priceRange)
}
