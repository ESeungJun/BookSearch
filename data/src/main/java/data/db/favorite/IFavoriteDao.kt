package data.db.favorite

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import data.db.book.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IFavoriteDao {
    // Room 의 Flow 쿼리는 favorite·book 테이블이 바뀔 때마다 다시 내보낸다 — 다른 화면에서 하트를 바꿔도 바로 반영된다
    @Query("SELECT book.* FROM favorite JOIN book ON book.`key` = favorite.bookKey ORDER BY favorite.savedAt DESC")
    fun observeAll(): Flow<List<BookEntity>>

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE bookKey = :key")
    suspend fun delete(key: String)
}
