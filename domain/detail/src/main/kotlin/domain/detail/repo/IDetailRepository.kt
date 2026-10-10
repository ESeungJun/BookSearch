package domain.detail.repo

import domain.base.data.BookDTO

interface IDetailRepository {
    /** 한 번이라도 받아 저장한 책. 검색 결과·즐겨찾기 어디서 열었든 같은 키로 찾는다. */
    suspend fun getBook(key: String): BookDTO?
}
