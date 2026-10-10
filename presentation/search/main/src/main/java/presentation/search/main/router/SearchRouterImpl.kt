package presentation.search.main.router

import core.navigation.INavigator
import presentation.router.SearchRouter
import javax.inject.Inject

class SearchRouterImpl @Inject constructor(
    private val navigator: INavigator,
) : SearchRouter() {

    override fun open() = navigator.navigate(PageData)
}
