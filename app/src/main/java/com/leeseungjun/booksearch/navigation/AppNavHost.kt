package com.leeseungjun.booksearch.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import core.navigation.EntryProviderInstaller

/** NavDisplay 에 넘기는 한 칸. 같은 경로(예: 같은 책 상세)가 두 탭에 있어도 키가 겹치지 않게 탭과 함께 둔다. */
data class TabRoute(val tab: String, val route: NavKey)

/**
 * 모든 탭의 백스택을 한 NavDisplay 로 그린다. 다른 탭의 항목도 목록에 남겨 두고 지금 탭을 맨 뒤에 둔다 —
 * 목록에서 빠진 항목은 "닫힌 화면"으로 처리돼 ViewModel·입력 상태가 지워지므로, 탭을 오가도 상태가 남게 하려는 것이다.
 * PageData → 화면 연결은 각 기능 main 모듈이 [entryInstallers] 로 내놓아서 이 파일은 기능 화면을 하나하나 알지 않는다.
 *
 * Navigation 3 를 고른 이유: 백스택이 우리가 가진 리스트라 탭별 백스택과 2칸 배치를 직접 다루기 쉽다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavHost(
    backStacks: Map<String, NavBackStack<NavKey>>,
    currentTab: String,
    navigator: AppNavigator,
    entryInstallers: Set<EntryProviderInstaller>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentStack = backStacks.getValue(currentTab)
    // 각 Router 는 navigator 로 이동한다. 지금 탭의 백스택을 가리키게 한다
    DisposableEffect(currentStack) {
        navigator.attach(currentStack)
        onDispose { navigator.detach(currentStack) }
    }
    val routes = backStacks.filterKeys { it != currentTab }.flatMap { (tab, stack) -> stack.map { TabRoute(tab, it) } } +
        currentStack.map { TabRoute(currentTab, it) }
    val routeEntries = remember(entryInstallers) {
        entryProvider<NavKey> { entryInstallers.forEach { install -> install() } }
    }
    val appSceneStrategy = rememberAppSceneStrategy()
    val sceneStrategy = remember(currentTab, appSceneStrategy) { CurrentTabSceneStrategy(currentTab, appSceneStrategy) }
    NavDisplay(
        backStack = routes,
        onBack = onBack,
        modifier = modifier,
        // 화면마다 rememberSaveable 상태와 ViewModel 을 항목 단위로 묶는다
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { tabRoute ->
            val entry = routeEntries(tabRoute.route)
            NavEntry(
                key = tabRoute,
                contentKey = "${tabRoute.tab}/${entry.contentKey}",
                metadata = entry.metadata + paneMetadata(tabRoute),
            ) { entry.Content() }
        },
        sceneStrategies = listOf(sceneStrategy),
        transitionSpec = { forwardTransition(this) },
        popTransitionSpec = { backTransition(this) },
        predictivePopTransitionSpec = { backTransition(this) },
    )
}
