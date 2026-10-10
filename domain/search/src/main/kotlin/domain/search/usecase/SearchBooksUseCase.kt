package domain.search.usecase

import domain.base.data.DomainResult
import domain.base.usecase.useCaseResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import javax.inject.Inject

/** 검색어와 정렬로 책을 찾는다. 새 검색·정렬 변경·새로고침 모두 첫 페이지부터 다시 받는 같은 행동이다. */
class SearchBooksUseCase @Inject constructor(
    private val repository: ISearchRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort): DomainResult<SearchPageDTO> =
        useCaseResult { repository.searchBooks(query, sort, FIRST_PAGE) }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
