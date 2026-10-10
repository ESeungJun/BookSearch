package com.leeseungjun.booksearch

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import com.leeseungjun.booksearch.navigation.AppNavHost
import core.navigation.EntryProviderInstaller
import com.leeseungjun.booksearch.navigation.AppNavigator
import presentation.router.FavoriteRouter
import presentation.router.SearchRouter

private enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    SEARCH(R.string.tab_search, Icons.Filled.Search),
    FAVORITE(R.string.tab_favorite, Icons.Filled.Favorite),
}

/**
 * 탭 골격. 탭마다 백스택을 따로 두어, 검색 탭에서 상세를 연 채 즐겨찾기 탭에 다녀와도 그 상세로 돌아온다.
 * NavigationSuiteScaffold 는 창 너비에 따라 하단 탭바와 측면 레일을 알아서 바꾼다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScaffold(navigator: AppNavigator, entryInstallers: Set<EntryProviderInstaller>, onExit: () -> Unit) {
    var currentTab by rememberSaveable { mutableStateOf(Tab.SEARCH) }
    val backStacks = mapOf(
        Tab.SEARCH to rememberNavBackStack(SearchRouter.PageData),
        Tab.FAVORITE to rememberNavBackStack(FavoriteRouter.PageData),
    )

    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val suiteState = rememberNavigationSuiteScaffoldState()
    // 키보드는 하단 탭바를 덮는다. 탭바가 남아 있으면 본문의 키보드 여백이 탭바 높이만큼 더 생기므로, 키보드가 떠 있는 동안 탭바를 숨긴다(측면 레일은 그대로)
    val hideBar = layoutType == NavigationSuiteType.NavigationBar && WindowInsets.isImeVisible
    LaunchedEffect(hideBar) { if (hideBar) suiteState.hide() else suiteState.show() }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        state = suiteState,
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
        AppNavHost(
            backStacks = backStacks.mapKeys { it.key.name },
            currentTab = currentTab.name,
            navigator = navigator,
            entryInstallers = entryInstallers,
            // 키보드가 목록 끝을 가리지 않게 본문을 키보드 위까지로 줄인다
            modifier = Modifier.imePadding(),
            onBack = {
                val stack = backStacks.getValue(currentTab)
                when {
                    stack.size > 1 -> stack.removeLastOrNull()
                    // 다른 탭의 첫 화면에서 뒤로 가면 검색 탭으로, 검색 탭의 첫 화면에서는 앱을 닫는다
                    currentTab != Tab.SEARCH -> currentTab = Tab.SEARCH
                    else -> onExit()
                }
            },
        )
    }
}
