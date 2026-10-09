package com.leeseungjun.booksearch.domain

import com.leeseungjun.booksearch.domain.model.Book
import com.leeseungjun.booksearch.domain.model.SearchPage
import com.leeseungjun.booksearch.domain.model.SearchSort
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    /** 네트워크를 먼저 시도하고, 3초를 넘기거나 실패하면 저장해 둔 결과를 돌려준다. 그것도 없으면 [com.leeseungjun.booksearch.domain.model.BookException]. */
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): Result<SearchPage>

    suspend fun getBook(key: String): Book?

    /** 즐겨찾기 목록. 최근에 추가한 것이 앞이다. */
    fun observeFavorites(): Flow<List<Book>>

    suspend fun addFavorite(book: Book)

    suspend fun removeFavorite(key: String)
}
