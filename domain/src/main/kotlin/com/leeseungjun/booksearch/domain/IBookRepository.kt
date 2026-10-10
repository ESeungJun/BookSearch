package com.leeseungjun.booksearch.domain

import com.leeseungjun.booksearch.domain.model.BookDTO
import com.leeseungjun.booksearch.domain.model.SearchConditionDTO
import com.leeseungjun.booksearch.domain.model.SearchPageDTO
import com.leeseungjun.booksearch.domain.model.SearchSort
import kotlinx.coroutines.flow.Flow

interface IBookRepository {
    /** 네트워크를 먼저 시도하고, 3초를 넘기거나 실패하면 저장해 둔 결과를 돌려준다. 그것도 없으면 [com.leeseungjun.booksearch.domain.model.BookException]. */
    suspend fun searchBooks(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO>

    suspend fun getBook(key: String): BookDTO?

    /** 결과를 받아 저장한 마지막 검색. 저장된 검색이 없으면 null. */
    suspend fun getLastSearch(): SearchConditionDTO?

    /** 즐겨찾기 목록. 최근에 추가한 것이 앞이다. */
    fun observeFavorites(): Flow<List<BookDTO>>

    suspend fun addFavorite(book: BookDTO)

    suspend fun removeFavorite(key: String)
}
