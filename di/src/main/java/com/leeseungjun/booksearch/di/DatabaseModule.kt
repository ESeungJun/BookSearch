package com.leeseungjun.booksearch.di

import android.content.Context
import androidx.room.Room
import com.leeseungjun.booksearch.data.db.BookDatabase
import com.leeseungjun.booksearch.data.db.book.BookDao
import com.leeseungjun.booksearch.data.db.favorite.FavoriteDao
import com.leeseungjun.booksearch.data.db.searchcache.SearchCacheDao
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
    fun provideDatabase(@ApplicationContext context: Context): BookDatabase =
        Room.databaseBuilder(context, BookDatabase::class.java, "book.db").build()

    @Provides
    fun provideBookDao(db: BookDatabase): BookDao = db.bookDao()

    @Provides
    fun provideFavoriteDao(db: BookDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideSearchCacheDao(db: BookDatabase): SearchCacheDao = db.searchCacheDao()
}
