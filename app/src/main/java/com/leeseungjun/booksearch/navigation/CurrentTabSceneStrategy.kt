package com.leeseungjun.booksearch.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy

/**
 * 뒤로 갈 곳(previousEntries)을 "마지막 항목 하나를 뺀 목록"으로 정하고, 지금 탭의 첫 화면이면 비운다.
 * 화면을 몇 칸으로 그릴지는 [strategy](ListDetailSceneStrategy)가 정하고, 맞는 것이 없으면 한 칸으로 그린다.
 * - NavDisplay 목록에는 탭 상태를 남기려고 다른 탭 항목도 앞에 들어 있다. ListDetailSceneStrategy 가 정한 뒤로 갈 곳을
 *   그대로 쓰면 탭 첫 화면에서도 NavDisplay 가 뒤로 가기를 받아, 뒤로 스와이프 미리보기에 다른 탭 화면이 보인다.
 * - 목록·상세 2칸은 한 장면이라, ListDetailSceneStrategy 는 상세만 닫는 것을 뒤로 가기로 보지 않고 장면 전체를 떠나는 곳(다른 탭)을 준다.
 * NavDisplay 는 뒤로 가기 한 번에 (전체 항목 수 − 이전 항목 수)번 onBack 을 부르므로, 마지막 하나만 빼야 한 번만 불린다.
 */
internal class CurrentTabSceneStrategy(
    private val currentTab: String,
    private val strategy: SceneStrategy<TabRoute>,
) : SceneStrategy<TabRoute> {
    private val fallback = SinglePaneSceneStrategy<TabRoute>()

    override fun SceneStrategyScope<TabRoute>.calculateScene(entries: List<NavEntry<TabRoute>>): Scene<TabRoute>? {
        val scene = with(strategy) { calculateScene(entries) } ?: with(fallback) { calculateScene(entries) }
        val hasPrevious = entries.count { it.tab == currentTab } > 1
        return CurrentTabScene(scene, if (hasPrevious) entries.dropLast(1) else emptyList())
    }
}

private data class CurrentTabScene(
    private val scene: Scene<TabRoute>,
    override val previousEntries: List<NavEntry<TabRoute>>,
) : Scene<TabRoute> by scene

/** 항목이 속한 탭. contentKey 를 "탭/경로" 로 만든다(AppNavHost). */
internal val NavEntry<*>.tab: String
    get() = contentKey.toString().substringBefore('/')
