package di.favorite

import data.repo.FavoriteRepositoryImpl
import domain.favorite.repo.IFavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
interface IFavoriteModule {
    @Binds
    @ViewModelScoped
    fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): IFavoriteRepository
}
