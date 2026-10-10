package com.leeseungjun.booksearch.domain.usecase

import com.leeseungjun.booksearch.domain.IBookRepository
import com.leeseungjun.booksearch.domain.model.SearchPageDTO
import com.leeseungjun.booksearch.domain.model.SearchSort
import javax.inject.Inject

/** 검색어로 책을 찾는다. 정렬을 고르고 다음 페이지를 이어 받을 수 있다. */
class SearchBooksUseCase @Inject constructor(
    private val repository: IBookRepository,
) {
    suspend operator fun invoke(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO> =
        repository.searchBooks(query.trim(), sort, page)
}
