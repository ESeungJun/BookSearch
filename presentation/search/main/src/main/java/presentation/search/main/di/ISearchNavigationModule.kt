package presentation.search.main.di

import core.navigation.EntryProviderInstaller
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import presentation.router.DetailRouter
import presentation.router.SearchRouter
import presentation.search.main.SearchScreen
import presentation.search.main.router.SearchRouterImpl

/**
 * 이 기능의 Router 구현을 바인딩하고, PageData → 화면 연결을 내놓는다(:app 이 모아 NavDisplay 에 넘긴다).
 * 백스택은 Activity 회전에도 이어지므로 Activity 가 다시 만들어져도 유지되는 범위에 둔다.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
interface ISearchNavigationModule {
    @Binds
    fun bindRouter(impl: SearchRouterImpl): SearchRouter

    companion object {
        @Provides
        @IntoSet
        fun provideEntry(detailRouter: DetailRouter): EntryProviderInstaller = {
            entry<SearchRouter.PageData> { SearchScreen(onBookClick = { detailRouter.open(DetailRouter.PageData(it)) }) }
        }
    }
}
