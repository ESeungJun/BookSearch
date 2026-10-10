package presentation.favorite.main.router

import core.navigation.INavigator
import presentation.router.FavoriteRouter
import javax.inject.Inject

class FavoriteRouterImpl @Inject constructor(
    private val navigator: INavigator,
) : FavoriteRouter() {

    override fun open() = navigator.navigate(PageData)
}
