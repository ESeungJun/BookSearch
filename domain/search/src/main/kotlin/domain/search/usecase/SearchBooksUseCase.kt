package domain.search.usecase

import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import javax.inject.Inject

/** 검색어와 정렬로 책을 찾는다. 새 검색·정렬 변경·새로고침은 1페이지, 목록 끝의 이어 받기는 다음 [page] 를 받는다. */
class SearchBooksUseCase @Inject constructor(
    private val repository: ISearchRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> =
        repository.searchBooks(query, sort, page)
}
