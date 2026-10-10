package data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import data.database.dao.IBookDao
import data.database.entity.BookEntity
import data.database.dao.IFavoriteDao
import data.database.entity.FavoriteEntity
import data.database.dao.ISearchCacheDao
import data.database.entity.SearchCacheEntity
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
        fun fromList(value: List<String>?): String? = value?.let { Json.encodeToString(it) }

        @TypeConverter
        fun toList(value: String?): List<String>? = value?.let { Json.decodeFromString(it) }
    }
}
