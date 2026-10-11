package data.api.searchbook.data

import data.base.db.entity.BookEntity
import data.base.db.entity.SearchCacheEntity

/**
 * 로컬 데이터 소스가 돌려주는 저장된 검색 페이지. 한 트랜잭션에서 읽은 DB 행 두 종류를 묶을 뿐이고 변환은 하지 않는다.
 * [info] 는 페이지 정보(총 개수·끝 여부·저장 시각)를 담은 캐시 행 하나, [books] 는 캐시 순서대로의 책 행.
 */
data class CachedPage(val info: SearchCacheEntity, val books: List<BookEntity>)
