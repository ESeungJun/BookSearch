package com.leeseungjun.booksearch.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.scene.Scene
import androidx.window.core.layout.WindowSizeClass
import com.leeseungjun.booksearch.R
import presentation.router.DetailRouter
import presentation.router.FavoriteRouter
import presentation.router.SearchRouter

/**
 * 창 너비 600dp 이상이면 목록과 상세를 한 화면에 나란히 둔다(기본값은 840dp 부터 2칸이라 중간 폭을 따로 연다).
 * 600dp 미만이면 한 칸이라 상세는 목록 위로 쌓인다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberAppSceneStrategy(): ListDetailSceneStrategy<TabRoute> {
    val windowInfo = currentWindowAdaptiveInfo()
    val directive = calculatePaneScaffoldDirective(windowInfo).let {
        val wide = windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
        it.copy(maxHorizontalPartitions = if (wide) TWO_PANES else ONE_PANE)
    }
    return rememberListDetailSceneStrategy(directive = directive)
}

/**
 * 어느 화면이 목록 칸이고 어느 화면이 상세 칸인지는 :app 이 경로 종류로 정한다. 기능 화면은 배치를 모른다.
 * 목록 칸에는 아직 고른 책이 없을 때 오른쪽 칸에 보일 안내를 함께 둔다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun paneMetadata(route: TabRoute): Map<String, Any> = when (route.route) {
    is SearchRouter.PageData, is FavoriteRouter.PageData -> ListDetailSceneStrategy.listPane(
        sceneKey = route.tab,
        detailPlaceholder = { DetailPlaceholder() },
    )
    is DetailRouter.PageData -> ListDetailSceneStrategy.detailPane(sceneKey = route.tab)
    else -> emptyMap()
}

@Composable
private fun DetailPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.detail_placeholder), style = MaterialTheme.typography.bodyLarge)
    }
}

/** 앞으로 갈 때는 오른쪽에서 들어오고, 뒤로 갈 때는 오른쪽으로 나간다. 탭을 바꾸는 것은 이동이 아니라 애니메이션 없이 바꾼다. */
internal fun forwardTransition(scope: AnimatedContentTransitionScope<Scene<TabRoute>>): ContentTransform =
    if (scope.isTabSwitch()) noTransition() else slideInHorizontally { it } togetherWith slideOutHorizontally { -it }

internal fun backTransition(scope: AnimatedContentTransitionScope<Scene<TabRoute>>): ContentTransform =
    if (scope.isTabSwitch()) noTransition() else slideInHorizontally { -it } togetherWith slideOutHorizontally { it }

private fun AnimatedContentTransitionScope<Scene<TabRoute>>.isTabSwitch(): Boolean =
    initialState.entries.lastOrNull()?.tab != targetState.entries.lastOrNull()?.tab

private fun noTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

private const val ONE_PANE = 1
private const val TWO_PANES = 2
