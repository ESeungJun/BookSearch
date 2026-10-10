package data.api.searchbook.di

import data.api.searchbook.repo.SearchRepositoryImpl
import data.api.searchbook.source.local.ISearchLocalDataSource
import data.api.searchbook.source.local.SearchLocalDataSourceImpl
import data.api.searchbook.source.remote.ISearchRemoteDataSource
import data.api.searchbook.source.remote.SearchRemoteDataSourceImpl
import domain.search.repo.ISearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** 저장소·데이터 소스는 상태가 없어 앱 전체에 하나일 필요가 없다. ViewModel 하나가 살아 있는 동안 같은 인스턴스를 쓴다. */
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
