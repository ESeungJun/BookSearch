package domain.usecase

import domain.repo.IBookRepository
import domain.data.SearchPageDTO
import domain.data.SearchSort
import javax.inject.Inject

/** 검색어로 책을 찾는다. 정렬을 고르고 다음 페이지를 이어 받을 수 있다. */
class SearchBooksUseCase @Inject constructor(
    private val repository: IBookRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO> =
        repository.searchBooks(query.trim(), sort, page)
}
