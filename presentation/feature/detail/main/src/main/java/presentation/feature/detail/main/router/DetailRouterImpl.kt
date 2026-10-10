package presentation.feature.detail.main.router

import core.navigation.INavigator
import presentation.router.DetailRouter
import javax.inject.Inject

class DetailRouterImpl @Inject constructor(
    private val navigator: INavigator,
) : DetailRouter() {

    // 이미 상세가 맨 위면(넓은 창에서 목록의 다른 책을 누른 경우) 쌓지 않고 바꾼다. 뒤로 가기 한 번에 목록으로 돌아간다
    override fun open(pageData: PageData) {
        if (navigator.current is PageData) navigator.replace(pageData) else navigator.navigate(pageData)
    }
}
