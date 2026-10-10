package presentation.router

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 즐겨찾기 탭의 시작 화면. 탭 전환으로만 열리므로(다른 화면이 이동해 오지 않는다) 여는 함수 없이 경로만 둔다. */
abstract class FavoriteRouter {
    @Serializable
    data object PageData : NavKey
}
