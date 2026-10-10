package data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import data.db.entity.BookEntity
import data.db.entity.SearchCacheEntity

@Dao
interface ISearchCacheDao {
    @Query("SELECT * FROM search_cache WHERE `query` = :query AND sort = :sort AND page = :page ORDER BY position")
    suspend fun getPage(query: String, sort: String, page: Int): List<SearchCacheEntity>

    /** 마지막으로 저장한 검색. 따로 테이블을 두지 않고 캐시의 저장 시각에서 찾는다. */
    @Query("SELECT * FROM search_cache ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLatest(): SearchCacheEntity?

    @Query(
        """
        SELECT book.* FROM search_cache JOIN book ON book.`key` = search_cache.bookKey
        WHERE `query` = :query AND sort = :sort AND page = :page ORDER BY position
        """,
    )
    suspend fun getBooks(query: String, sort: String, page: Int): List<BookEntity>

    /**
     * 한 페이지를 저장한다. 1페이지는 새 검색이므로 그 조합의 옛 페이지를 모두 지운다 —
     * 새 1페이지와 예전에 받은 2~5페이지가 섞이지 않게 한다.
     */
    @Transaction
    suspend fun savePage(query: String, sort: String, page: Int, rows: List<SearchCacheEntity>, maxCombinations: Int) {
        if (page == 1) deleteCombination(query, sort) else deletePage(query, sort, page)
        insert(rows)
        deleteOldCombinations(maxCombinations)
    }

    @Insert
    suspend fun insert(rows: List<SearchCacheEntity>)

    @Query("DELETE FROM search_cache WHERE `query` = :query AND sort = :sort")
    suspend fun deleteCombination(query: String, sort: String)

    @Query("DELETE FROM search_cache WHERE `query` = :query AND sort = :sort AND page = :page")
    suspend fun deletePage(query: String, sort: String, page: Int)

    /** 가장 최근에 저장한 (검색어, 정렬) 조합 [max] 개만 남긴다(LRU). */
    @Query(
        """
        DELETE FROM search_cache WHERE `query` || '|' || sort NOT IN (
            SELECT `query` || '|' || sort FROM search_cache
            GROUP BY `query`, sort ORDER BY MAX(savedAt) DESC LIMIT :max
        )
        """,
    )
    suspend fun deleteOldCombinations(max: Int)
}
