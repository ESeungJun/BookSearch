package data.base.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import data.base.db.entity.BookEntity
import data.base.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

// Room 의 Flow 쿼리는 favorite·book 테이블이 바뀔 때마다 다시 내보낸다 — 다른 화면에서 하트를 바꿔도 바로 반영된다
@Dao
interface IFavoriteDao {
    /**
     * 즐겨찾기를 검색어·금액 범위로 고른다. 최근에 넣은 것이 앞이고, 다른 순서는 화면이 정한다.
     * [pattern] 은 LIKE 패턴(`%검색어%`, `\` 로 이스케이프)이고 null 이면 검색어 조건을 쓰지 않는다.
     * 저자는 JSON 문자열 한 칸에 있어 LIKE 로 함께 찾는다. 금액은 실제로 내는 가격(0보다 큰 할인가, 아니면 정가) 기준이고 — 카드에 보이는 가격과 같은 기준이다 —
     * 범위를 고르면 가격 정보가 없는 책은 빠진다(범위 안인지 알 수 없다).
     */
    @Query(
        """
        SELECT book.* FROM favorite JOIN book ON book.`key` = favorite.bookKey
        WHERE (:pattern IS NULL OR book.title LIKE :pattern ESCAPE '\' OR book.authors LIKE :pattern ESCAPE '\')
          AND (:minPrice IS NULL OR (CASE WHEN book.salePrice > 0 THEN book.salePrice ELSE book.price END) >= :minPrice)
          AND (:maxPrice IS NULL OR (CASE WHEN book.salePrice > 0 THEN book.salePrice ELSE book.price END) <= :maxPrice)
        ORDER BY favorite.savedAt DESC
        """,
    )
    fun observe(pattern: String?, minPrice: Int?, maxPrice: Int?): Flow<List<BookEntity>>

    @Query("SELECT bookKey FROM favorite")
    fun observeKeys(): Flow<List<String>>

    /**
     * 책 정보와 즐겨찾기를 한 트랜잭션으로 저장한다. 따로 저장하면 그 사이에 새 검색이 "참조 없는 책"을 지워
     * 즐겨찾기가 책 없이 남고, 목록(book 과 JOIN)에서 사라질 수 있다.
     */
    @Transaction
    suspend fun add(book: BookEntity, favorite: FavoriteEntity) {
        upsertBook(book)
        upsert(favorite)
    }

    @Upsert
    suspend fun upsertBook(book: BookEntity)

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE bookKey = :key")
    suspend fun delete(key: String)
}
