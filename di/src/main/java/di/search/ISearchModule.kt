package di.search

import data.repo.SearchRepositoryImpl
import data.source.remote.BookRemoteDataSourceImpl
import data.source.remote.IBookRemoteDataSource
import domain.search.repo.ISearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** 원격 데이터 소스는 검색만 쓴다. 범위는 di.book.IBookModule 과 같은 이유로 ViewModel 이다. */
@Module
@InstallIn(ViewModelComponent::class)
interface ISearchModule {
    @Binds
    @ViewModelScoped
    fun bindSearchRepository(impl: SearchRepositoryImpl): ISearchRepository

    @Binds
    @ViewModelScoped
    fun bindRemoteDataSource(impl: BookRemoteDataSourceImpl): IBookRemoteDataSource
}
