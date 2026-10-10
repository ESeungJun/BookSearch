package domain.search.usecase

import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import javax.inject.Inject

/** 같은 검색의 다음 페이지를 이어 받는다. [page] 는 받을 페이지 번호(2부터)다. */
class LoadMoreBooksUseCase @Inject constructor(
    private val repository: ISearchRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> =
        repository.searchBooks(query, sort, page)
}
