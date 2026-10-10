package core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** 기능 모듈이 자기 경로 → 화면 연결을 등록하는 함수. 화면에서 이동할 때 쓰도록 [INavigator] 를 받는다. */
typealias EntryProviderInstaller = EntryProviderScope<NavKey>.(navigator: INavigator) -> Unit
