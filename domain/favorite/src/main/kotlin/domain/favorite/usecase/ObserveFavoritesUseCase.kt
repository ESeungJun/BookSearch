package domain.favorite.usecase

import domain.favorite.repo.IFavoriteRepository
import domain.book.data.BookDTO
import domain.favorite.data.FavoriteSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 즐겨찾기한 책을 검색어·제목 정렬·금액 범위로 골라 본다. 모두 로컬 데이터만 쓴다.
 * 즐겨찾기는 많아야 수백 권이라 DB 쿼리 대신 메모리에서 거른다 — 규칙이 한 함수에 모여 테스트하기 쉽다.
 */
class ObserveFavoritesUseCase @Inject constructor(
    private val repository: IFavoriteRepository,
) {
    operator fun invoke(query: String, sort: FavoriteSort, priceRange: IntRange?): Flow<List<BookDTO>> =
        repository.observeFavorites().map { books -> filterAndSort(books, query.trim(), sort, priceRange) }

    private fun filterAndSort(books: List<BookDTO>, query: String, sort: FavoriteSort, priceRange: IntRange?): List<BookDTO> {
        val filtered = books.filter { book ->
            val matchesQuery = query.isEmpty() ||
                book.title?.contains(query, ignoreCase = true) == true ||
                book.authors.orEmpty().any { it.contains(query, ignoreCase = true) }
            // 가격 정보가 없는 책은 금액 범위를 고르면 빠진다(범위 안인지 알 수 없다)
            val matchesPrice = priceRange == null || book.displayPrice?.let { it in priceRange } == true
            matchesQuery && matchesPrice
        }
        // 제목이 없는 책은 이름순 비교가 안 되므로 정렬 방향과 관계없이 맨 뒤에 둔다
        return when (sort) {
            FavoriteSort.TITLE_ASC -> filtered.sortedWith(compareBy(nullsLast()) { it.title })
            FavoriteSort.TITLE_DESC -> filtered.sortedWith(compareBy(nullsLast(reverseOrder())) { it.title })
        }
    }
}
