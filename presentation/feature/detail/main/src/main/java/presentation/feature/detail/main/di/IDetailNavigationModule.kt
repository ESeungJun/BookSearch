package presentation.feature.detail.main.di

import core.navigation.EntryProviderInstaller
import core.navigation.INavigator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import presentation.feature.detail.main.router.DetailRouterImpl
import presentation.feature.detail.main.view.DetailScreen
import presentation.router.DetailRouter

/**
 * 이 기능의 Router 구현을 바인딩하고, PageData → 화면 연결을 내놓는다(:app 이 모아 NavDisplay 에 넘긴다).
 * 주입받는 INavigator(AppNavigator)가 ActivityRetainedComponent 범위라 같은 범위에 둔다.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
interface IDetailNavigationModule {
    @Binds
    fun bindRouter(impl: DetailRouterImpl): DetailRouter

    companion object {
        @Provides
        @IntoSet
        fun provideEntry(navigator: INavigator): EntryProviderInstaller = {
            entry<DetailRouter.PageData> { pageData ->
                DetailScreen(bookId = pageData.bookId, onBack = navigator::back)
            }
        }
    }
}
