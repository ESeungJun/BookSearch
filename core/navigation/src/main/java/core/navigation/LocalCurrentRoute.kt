package core.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey

/**
 * 지금 탭에서 맨 위에 보이는 화면의 경로. :app 이 화면들에 내려준다.
 * 넓은 창에서 목록과 상세가 함께 보일 때 목록이 지금 열린 책을 표시하는 데 쓴다.
 */
val LocalCurrentRoute = compositionLocalOf<NavKey?> { null }
