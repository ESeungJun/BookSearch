package com.leeseungjun.booksearch

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import com.leeseungjun.booksearch.navigation.AppNavHost
import core.navigation.EntryProviderInstaller
import presentation.favorite.route.FavoriteRoute
import presentation.search.route.SearchRoute

private enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    SEARCH(R.string.tab_search, Icons.Filled.Search),
    FAVORITE(R.string.tab_favorite, Icons.Filled.Favorite),
}

/**
 * 탭 골격. 탭마다 백스택을 따로 두어, 검색 탭에서 상세를 연 채 즐겨찾기 탭에 다녀와도 그 상세로 돌아온다.
 * NavigationSuiteScaffold 는 창 너비에 따라 하단 탭바와 측면 레일을 알아서 바꾼다.
 */
@Composable
fun MainScaffold(entryInstallers: Set<EntryProviderInstaller>) {
    var currentTab by rememberSaveable { mutableStateOf(Tab.SEARCH) }
    val backStacks = mapOf(
        Tab.SEARCH to rememberNavBackStack(SearchRoute),
        Tab.FAVORITE to rememberNavBackStack(FavoriteRoute),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Tab.entries.forEach { tab ->
                item(
                    selected = tab == currentTab,
                    onClick = { currentTab = tab },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(stringResource(tab.label)) },
                )
            }
        },
    ) {
        AppNavHost(backStack = backStacks.getValue(currentTab), entryInstallers = entryInstallers)
    }
}
