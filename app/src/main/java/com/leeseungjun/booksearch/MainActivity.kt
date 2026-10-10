package com.leeseungjun.booksearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import core.designsystem.BookSearchTheme
import core.navigation.EntryProviderInstaller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 앱의 유일한 Activity. 넓은 창에서 목록과 상세를 한 화면에 나란히 두려면
 * 두 화면이 같은 Compose 트리에 있어야 해서 Activity 를 나누지 않는다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 각 기능 main 모듈이 Hilt 로 내놓은 경로 → 화면 연결
    @Inject
    lateinit var entryInstallers: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookSearchTheme {
                MainScaffold(entryInstallers)
            }
        }
    }
}
