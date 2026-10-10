package data.local.favorite.di

import data.local.favorite.repo.FavoriteRepositoryImpl
import data.local.favorite.source.local.FavoriteLocalDataSourceImpl
import data.local.favorite.source.local.IFavoriteLocalDataSource
import domain.favorite.repo.IFavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** 저장소·데이터 소스는 상태가 없어 앱 전체에 하나일 필요가 없다. ViewModel 하나가 살아 있는 동안 같은 인스턴스를 쓴다. */
@Module
@InstallIn(ViewModelComponent::class)
interface IFavoriteModule {
    @Binds
    @ViewModelScoped
    fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): IFavoriteRepository

    @Binds
    @ViewModelScoped
    fun bindLocalDataSource(impl: FavoriteLocalDataSourceImpl): IFavoriteLocalDataSource
}
