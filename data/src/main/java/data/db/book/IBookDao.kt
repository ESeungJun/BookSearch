package data.db.book

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface IBookDao {
    @Upsert
    suspend fun upsert(books: List<BookEntity>)

    @Query("SELECT * FROM book WHERE `key` = :key")
    suspend fun get(key: String): BookEntity?

    /** 즐겨찾기도 검색 캐시도 가리키지 않는 책을 지운다. 캐시가 밀려날 때마다 불러 테이블이 끝없이 커지지 않게 한다. */
    @Query(
        """
        DELETE FROM book
        WHERE `key` NOT IN (SELECT bookKey FROM favorite)
          AND `key` NOT IN (SELECT bookKey FROM search_cache)
        """,
    )
    suspend fun deleteUnreferenced()
}
