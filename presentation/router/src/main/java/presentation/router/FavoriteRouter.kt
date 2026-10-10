package presentation.router

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 즐겨찾기 탭. 탭의 시작 화면이라 넘길 값이 없다. */
abstract class FavoriteRouter {
    @Serializable
    data object PageData : NavKey

    abstract fun open()
}
