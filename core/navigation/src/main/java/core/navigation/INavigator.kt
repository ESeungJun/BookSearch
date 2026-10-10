package core.navigation

import androidx.navigation3.runtime.NavKey

/** 화면 이동. 각 RouterImpl 이 주입받아 쓰고, 구현은 탭별 백스택을 가진 :app 이 한다. */
interface INavigator {
    /** 지금 탭에서 맨 위에 보이는 화면의 경로. */
    val current: NavKey?

    fun navigate(route: NavKey)

    /** 맨 위 화면을 [route] 로 바꾼다(쌓지 않는다). */
    fun replace(route: NavKey)
    fun back()
}
