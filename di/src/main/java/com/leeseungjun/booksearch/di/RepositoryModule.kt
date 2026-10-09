package com.leeseungjun.booksearch.di

import com.leeseungjun.booksearch.data.repository.BookRepositoryImpl
import com.leeseungjun.booksearch.domain.BookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindBookRepository(impl: BookRepositoryImpl): BookRepository
}
