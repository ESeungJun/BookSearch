package presentation.feature.search.main.di

import core.navigation.EntryProviderInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import presentation.router.DetailRouter
import presentation.router.SearchRouter
import presentation.feature.search.main.view.SearchScreen

/**
 * PageData → 화면 연결을 내놓는다(:app 이 모아 NavDisplay 에 넘긴다). 탭 시작 화면이라 Router 구현은 없다.
 * 백스택은 Activity 회전에도 이어지므로 Activity 가 다시 만들어져도 유지되는 범위에 둔다.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
interface ISearchNavigationModule {
    companion object {
        @Provides
        @IntoSet
        fun provideEntry(detailRouter: DetailRouter): EntryProviderInstaller = {
            entry<SearchRouter.PageData> { SearchScreen(onBookClick = { detailRouter.open(DetailRouter.PageData(it)) }) }
        }
    }
}
