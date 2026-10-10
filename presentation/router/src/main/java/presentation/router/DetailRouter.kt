package presentation.router

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 책 상세. [PageData.bookId] 는 책의 매칭 키다. */
abstract class DetailRouter {
    @Serializable
    data class PageData(val bookId: String) : NavKey

    abstract fun open(pageData: PageData)
}
