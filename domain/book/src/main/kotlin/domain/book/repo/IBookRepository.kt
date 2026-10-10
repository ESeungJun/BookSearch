package domain.book.repo

import domain.book.data.BookDTO

interface IBookRepository {
    /** 한 번이라도 받아 저장한 책. 검색 결과·즐겨찾기 어디서 열었든 같은 키로 찾는다(D-08). */
    suspend fun getBook(key: String): BookDTO?
}
