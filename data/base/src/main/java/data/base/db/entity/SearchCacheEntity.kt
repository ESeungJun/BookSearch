package data.base.db.entity

import androidx.room.Entity

/**
 * 검색 결과 캐시. (검색어, 정렬, 페이지) 안의 책 한 권이 한 행이다.
 * 책 정보 대신 [bookKey] 만 두어, 같은 책이 여러 검색에 나와도 book 테이블에 한 번만 저장된다.
 * 페이지 정보(총 개수·끝 여부·저장 시각)는 같은 페이지의 행마다 같은 값으로 들어간다.
 */
@Entity(tableName = "search_cache", primaryKeys = ["query", "sort", "page", "position"])
data class SearchCacheEntity(
    val query: String,
    val sort: String,
    val page: Int,
    val position: Int,
    val bookKey: String,
    val totalCount: Int,
    val isEnd: Boolean,
    val savedAt: Long,
)
