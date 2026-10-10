package com.leeseungjun.booksearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import core.designsystem.BookSearchTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱의 유일한 Activity. 넓은 창에서 목록과 상세를 한 화면에 나란히 두려면
 * 두 화면이 같은 Compose 트리에 있어야 해서 Activity 를 나누지 않는다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookSearchTheme {
                MainScaffold()
            }
        }
    }
}
