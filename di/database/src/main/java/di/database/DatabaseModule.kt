package di.database

import android.content.Context
import androidx.room.Room
import data.database.AbsBookDatabase
import data.database.dao.IBookDao
import data.database.dao.IFavoriteDao
import data.database.dao.ISearchCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DB 는 앱에 하나여야 한다. Room 의 변경 감지(Flow)는 인스턴스 단위라, 화면마다 따로 열면
 * 즐겨찾기 탭에서 바꾼 내용이 검색 탭 하트에 반영되지 않는다. DAO 는 DB 에서 꺼내기만 하므로 범위를 두지 않는다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AbsBookDatabase =
        Room.databaseBuilder(context, AbsBookDatabase::class.java, "book.db").build()

    @Provides
    fun provideBookDao(db: AbsBookDatabase): IBookDao = db.bookDao()

    @Provides
    fun provideFavoriteDao(db: AbsBookDatabase): IFavoriteDao = db.favoriteDao()

    @Provides
    fun provideSearchCacheDao(db: AbsBookDatabase): ISearchCacheDao = db.searchCacheDao()
}
