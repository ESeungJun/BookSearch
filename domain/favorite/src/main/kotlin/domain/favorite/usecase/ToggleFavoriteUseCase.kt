package domain.favorite.usecase

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.base.usecase.useCase
import domain.favorite.repo.IFavoriteRepository
import javax.inject.Inject

/** 책을 즐겨찾기에 넣거나 뺀다. 검색 목록·즐겨찾기 목록·상세 어디서든 같은 행동이다. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: IFavoriteRepository,
) {
    suspend operator fun invoke(book: BookDTO, isFavorite: Boolean): DomainResult<Unit> =
        useCase { repository.toggleFavorite(book, isFavorite) }
}
