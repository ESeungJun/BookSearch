package presentation.feature.detail.main.router

import core.navigation.INavigator
import presentation.router.DetailRouter
import javax.inject.Inject

class DetailRouterImpl @Inject constructor(
    private val navigator: INavigator,
) : DetailRouter() {

    override fun open(pageData: PageData) = navigator.navigate(pageData)
}
