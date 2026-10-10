package core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import core.database.entity.BookEntity

@Dao
interface IBookDao {
    @Upsert
    suspend fun upsert(books: List<BookEntity>)

    @Query("SELECT * FROM book WHERE `key` = :key")
    suspend fun get(key: String): BookEntity?

    /**
     * 기기에 저장된 책(최근 검색 결과·즐겨찾기) 가운데 제목·저자에 [pattern] 이 들어간 책.
     * [pattern] 은 LIKE 패턴(`%검색어%`, `\` 로 이스케이프)이다. 저자는 JSON 문자열 한 칸에 있어 LIKE 로 함께 찾는다.
     * [latest] 면 출간일 최신순, 아니면 제목에 들어간 책을 먼저 두고 제목순이다(서버의 정확도 순서는 기기에서 알 수 없다).
     */
    @Query(
        """
        SELECT * FROM book
        WHERE title LIKE :pattern ESCAPE '\' OR authors LIKE :pattern ESCAPE '\'
        ORDER BY
          CASE WHEN :latest THEN publishedDate END DESC,
          CASE WHEN title LIKE :pattern ESCAPE '\' THEN 0 ELSE 1 END,
          title
        LIMIT :limit
        """,
    )
    suspend fun search(pattern: String, latest: Boolean, limit: Int): List<BookEntity>

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
