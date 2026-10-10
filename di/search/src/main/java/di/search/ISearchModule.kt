package di.search

import data.search.repo.SearchRepositoryImpl
import data.search.source.local.ISearchLocalDataSource
import data.search.source.local.SearchLocalDataSourceImpl
import data.search.source.remote.ISearchRemoteDataSource
import data.search.source.remote.SearchRemoteDataSourceImpl
import domain.search.repo.ISearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** 범위는 di.detail.IDetailModule 과 같은 이유로 ViewModel 이다. */
@Module
@InstallIn(ViewModelComponent::class)
interface ISearchModule {
    @Binds
    @ViewModelScoped
    fun bindSearchRepository(impl: SearchRepositoryImpl): ISearchRepository

    @Binds
    @ViewModelScoped
    fun bindRemoteDataSource(impl: SearchRemoteDataSourceImpl): ISearchRemoteDataSource

    @Binds
    @ViewModelScoped
    fun bindLocalDataSource(impl: SearchLocalDataSourceImpl): ISearchLocalDataSource
}
