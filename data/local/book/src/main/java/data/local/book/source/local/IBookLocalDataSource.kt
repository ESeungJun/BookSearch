package data.local.book.source.local

import domain.base.data.BookDTO

/** 기기에 저장한 책 정보만 맡는다. 검색 캐시·즐겨찾기는 각 기능 모듈의 로컬 데이터 소스가 맡는다. */
interface IBookLocalDataSource {
    suspend fun getBook(key: String): BookDTO?
}
