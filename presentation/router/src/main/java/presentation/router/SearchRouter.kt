package presentation.router

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 검색 탭. 탭의 시작 화면이라 넘길 값이 없다. */
abstract class SearchRouter {
    @Serializable
    data object PageData : NavKey

    abstract fun open()
}
