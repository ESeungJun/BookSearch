package com.leeseungjun.booksearch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import core.navigation.EntryProviderInstaller

/**
 * 한 탭의 백스택을 화면으로 그린다. PageData → 화면 연결은 각 기능 main 모듈이 [entryInstallers] 로 내놓아서
 * 이 파일은 기능 화면을 하나하나 알지 않는다.
 *
 * Navigation 3 를 고른 이유: 백스택이 우리가 가진 리스트라 탭별 백스택과 2칸 배치를 직접 다루기 쉽다.
 */
@Composable
fun AppNavHost(
    backStack: NavBackStack<NavKey>,
    navigator: AppNavigator,
    entryInstallers: Set<EntryProviderInstaller>,
    modifier: Modifier = Modifier,
) {
    // 각 Router 는 navigator 로 이동한다. 지금 보이는 탭의 백스택을 가리키게 한다
    DisposableEffect(backStack) {
        navigator.attach(backStack)
        onDispose { navigator.detach(backStack) }
    }
    NavDisplay(
        backStack = backStack,
        onBack = { navigator.back() },
        modifier = modifier,
        // 화면마다 rememberSaveable 상태와 ViewModel 을 백스택 항목 단위로 묶는다
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entryInstallers.forEach { install -> install() }
        },
    )
}
