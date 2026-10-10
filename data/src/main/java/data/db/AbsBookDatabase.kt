package data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import data.db.dao.IBookDao
import data.db.entity.BookEntity
import data.db.dao.IFavoriteDao
import data.db.entity.FavoriteEntity
import data.db.dao.ISearchCacheDao
import data.db.entity.SearchCacheEntity
import kotlinx.serialization.json.Json

/**
 * 아직 배포 전이라 버전 1 에서 시작하고 스키마 파일을 내보내지 않는다(exportSchema = false).
 * 첫 배포 후 테이블을 바꿀 때 exportSchema 를 켜고 마이그레이션을 추가한다.
 */
@Database(
    entities = [BookEntity::class, FavoriteEntity::class, SearchCacheEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(AbsBookDatabase.Converters::class)
abstract class AbsBookDatabase : RoomDatabase() {
    abstract fun bookDao(): IBookDao
    abstract fun favoriteDao(): IFavoriteDao
    abstract fun searchCacheDao(): ISearchCacheDao

    class Converters {
        // 저자 목록은 검색 조건으로 쓰지 않아 별도 테이블 대신 JSON 문자열 한 칸에 둔다
        @TypeConverter
        fun fromList(value: List<String>): String = Json.encodeToString(value)

        @TypeConverter
        fun toList(value: String): List<String> = Json.decodeFromString(value)
    }
}
