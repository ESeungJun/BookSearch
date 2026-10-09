package com.leeseungjun.booksearch.domain.usecase

import com.leeseungjun.booksearch.domain.BookRepository
import com.leeseungjun.booksearch.domain.model.Book
import javax.inject.Inject

/** 책을 즐겨찾기에 넣거나 뺀다. 검색 목록·즐겨찾기 목록·상세 어디서든 같은 행동이다. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    suspend operator fun invoke(book: Book, isFavorite: Boolean) {
        if (isFavorite) repository.removeFavorite(book.key) else repository.addFavorite(book)
    }
}
