package domain.search.usecase

import domain.search.repo.ISearchRepository
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import javax.inject.Inject

/** 검색어로 책을 찾는다. 정렬을 고르고 다음 페이지를 이어 받을 수 있다. */
class SearchBooksUseCase @Inject constructor(
    private val repository: ISearchRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO> =
        repository.searchBooks(query.trim(), sort, page)
}
