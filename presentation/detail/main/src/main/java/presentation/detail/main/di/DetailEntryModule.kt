package presentation.detail.main.di

import core.navigation.EntryProviderInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import presentation.detail.main.DetailScreen
import presentation.detail.route.DetailRoute

/** 화면을 경로에 연결해 내놓는다. :app 이 모든 기능의 연결을 모아 NavDisplay 에 넘긴다. */
@Module
@InstallIn(SingletonComponent::class)
object DetailEntryModule {
    @Provides
    @IntoSet
    fun provideDetailEntry(): EntryProviderInstaller = { _ ->
        entry<DetailRoute> { route -> DetailScreen(bookId = route.bookId) }
    }
}
