package com.leeseungjun.booksearch.di

import com.leeseungjun.booksearch.data.repository.BookRepositoryImpl
import com.leeseungjun.booksearch.domain.IBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface IRepositoryModule {
    @Binds
    @Singleton
    fun bindBookRepository(impl: BookRepositoryImpl): IBookRepository
}
