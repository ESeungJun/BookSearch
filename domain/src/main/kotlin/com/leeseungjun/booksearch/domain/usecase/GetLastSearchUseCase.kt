package com.leeseungjun.booksearch.domain.usecase

import com.leeseungjun.booksearch.domain.BookRepository
import com.leeseungjun.booksearch.domain.model.SearchCondition
import javax.inject.Inject

/**
 * 앱을 다시 열면 마지막으로 본 검색을 이어서 본다(U-13).
 * 첫 화면이 비어 있으면 지하철처럼 오프라인으로 앱을 켰을 때 볼 것이 없다 — 마지막 검색을 다시 부르면
 * 네트워크가 되면 새 결과를, 안 되면 저장해 둔 결과를 보게 된다.
 */
class GetLastSearchUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    suspend operator fun invoke(): SearchCondition? = repository.getLastSearch()
}
