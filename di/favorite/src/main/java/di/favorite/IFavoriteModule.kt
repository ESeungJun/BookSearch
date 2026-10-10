package di.favorite

import data.favorite.repo.FavoriteRepositoryImpl
import data.favorite.source.local.FavoriteLocalDataSourceImpl
import data.favorite.source.local.IFavoriteLocalDataSource
import domain.favorite.repo.IFavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** 범위는 di.book.IBookModule 과 같은 이유로 ViewModel 이다. */
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
