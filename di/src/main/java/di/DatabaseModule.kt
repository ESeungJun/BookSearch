package di

import android.content.Context
import androidx.room.Room
import data.db.AbsBookDatabase
import data.db.book.IBookDao
import data.db.favorite.IFavoriteDao
import data.db.searchcache.ISearchCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
