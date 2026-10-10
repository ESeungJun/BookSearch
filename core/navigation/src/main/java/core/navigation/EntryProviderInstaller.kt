package core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** 기능 main 모듈이 자기 PageData → 화면 연결을 등록하는 함수. :app 이 모아 NavDisplay 에 넘긴다. */
typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit
