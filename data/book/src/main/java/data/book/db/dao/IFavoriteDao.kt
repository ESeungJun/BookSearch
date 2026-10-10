package data.book.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import data.book.db.entity.BookEntity
import data.book.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

// Room 의 Flow 쿼리는 favorite·book 테이블이 바뀔 때마다 다시 내보낸다 — 다른 화면에서 하트를 바꿔도 바로 반영된다
@Dao
interface IFavoriteDao {
    /**
     * 즐겨찾기를 검색어·금액 범위로 고른다. 최근에 넣은 것이 앞이고, 다른 순서는 화면이 정한다.
     * [pattern] 은 LIKE 패턴(`%검색어%`, `\` 로 이스케이프)이고 null 이면 검색어 조건을 쓰지 않는다.
     * 저자는 JSON 문자열 한 칸에 있어 LIKE 로 함께 찾는다. 금액은 실제로 내는 가격(할인가, 없으면 정가) 기준이고,
     * 범위를 고르면 가격 정보가 없는 책은 빠진다(범위 안인지 알 수 없다).
     */
    @Query(
        """
        SELECT book.* FROM favorite JOIN book ON book.`key` = favorite.bookKey
        WHERE (:pattern IS NULL OR book.title LIKE :pattern ESCAPE '\' OR book.authors LIKE :pattern ESCAPE '\')
          AND (:minPrice IS NULL OR COALESCE(book.salePrice, book.price) >= :minPrice)
          AND (:maxPrice IS NULL OR COALESCE(book.salePrice, book.price) <= :maxPrice)
        ORDER BY favorite.savedAt DESC
        """,
    )
    fun observe(pattern: String?, minPrice: Int?, maxPrice: Int?): Flow<List<BookEntity>>

    @Query("SELECT bookKey FROM favorite")
    fun observeKeys(): Flow<List<String>>

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE bookKey = :key")
    suspend fun delete(key: String)
}
