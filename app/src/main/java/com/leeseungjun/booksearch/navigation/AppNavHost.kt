package com.leeseungjun.booksearch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import presentation.detail.DetailScreen
import presentation.favorite.FavoriteScreen
import presentation.search.SearchScreen

/**
 * 경로 → 화면 연결. 화면 모듈은 서로를 모르고 콜백만 내놓으며, 어디로 갈지는 여기서 정한다.
 *
 * Navigation 3 를 고른 이유: 백스택이 우리가 가진 리스트라 탭별 백스택과 2칸 배치를 직접 다루기 쉽다.
 */
@Composable
fun AppNavHost(backStack: NavBackStack<NavKey>, modifier: Modifier = Modifier) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        // 화면마다 rememberSaveable 상태와 ViewModel 을 백스택 항목 단위로 묶는다
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SearchRoute> { SearchScreen(onBookClick = { backStack.add(DetailRoute(it)) }) }
            entry<FavoriteRoute> { FavoriteScreen(onBookClick = { backStack.add(DetailRoute(it)) }) }
            entry<DetailRoute> { route -> DetailScreen(bookId = route.bookId) }
        },
    )
}
