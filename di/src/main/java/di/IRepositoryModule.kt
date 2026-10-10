package di

import data.repository.BookRepositoryImpl
import domain.repo.IBookRepository
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
