package com.leeseungjun.booksearch.domain.usecase

import com.leeseungjun.booksearch.domain.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 어떤 책이 즐겨찾기인지 본다. 다른 화면에서 하트를 바꿔도 바로 반영되도록 계속 관찰한다.
 * 검색 결과에는 즐겨찾기 여부를 저장하지 않고 이 값에서 계산한다 — 두 곳에 두면 서로 어긋날 수 있다.
 */
class ObserveFavoriteKeysUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    operator fun invoke(): Flow<Set<String>> =
        repository.observeFavorites().map { books -> books.mapTo(HashSet()) { it.key } }
}
