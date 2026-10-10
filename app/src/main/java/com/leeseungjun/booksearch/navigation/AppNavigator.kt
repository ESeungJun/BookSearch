package com.leeseungjun.booksearch.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import core.navigation.INavigator
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * 지금 화면에 보이는 탭의 백스택에 이동을 전한다. 백스택 자체는 화면(rememberNavBackStack)이 저장·복원하고,
 * 이 객체는 그것을 가리키기만 해서 상태를 따로 갖지 않는다. Activity 가 다시 만들어지면 새 백스택을 다시 가리킨다.
 */
@ActivityRetainedScoped
class AppNavigator @Inject constructor() : INavigator {

    private var backStack: NavBackStack<NavKey>? = null

    fun attach(backStack: NavBackStack<NavKey>) {
        this.backStack = backStack
    }

    fun detach(backStack: NavBackStack<NavKey>) {
        if (this.backStack === backStack) this.backStack = null
    }

    override val current: NavKey?
        get() = backStack?.lastOrNull()

    override fun replace(route: NavKey) {
        val stack = backStack ?: return
        stack.removeLastOrNull()
        stack.add(route)
    }

    override fun navigate(route: NavKey) {
        backStack?.add(route)
    }

    override fun back() {
        backStack?.removeLastOrNull()
    }
}
