package core.navigation

import androidx.navigation3.runtime.NavKey

/** 화면 이동. 구현은 탭별 백스택을 가진 :app 이 한다. */
interface INavigator {
    fun navigate(route: NavKey)
    fun back()
}
