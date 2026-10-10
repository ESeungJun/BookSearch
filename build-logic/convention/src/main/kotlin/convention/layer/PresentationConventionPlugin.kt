package convention.layer

import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 기능 화면 모듈(:presentation:<기능>:main) 공통 구성. 화면은 :core:designsystem·:core:navigation·:domain:base·
 * :presentation:base(책 카드처럼 domain 타입을 아는 공통 UI)와 각 모듈이 적은 :domain:<기능>·이동 대상의 route 모듈만 안다.
 */
class PresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("convention.compose")
        pluginManager.apply("convention.hilt")
        listOf(
            "androidx-lifecycle-runtime-compose",
            "androidx-lifecycle-viewmodel-compose",
            "hilt-lifecycle-viewmodel-compose",
            "kotlinx-collections-immutable",
            "kotlinx-coroutines-core",
            "nav3-runtime",
        ).forEach { dependencies.add("implementation", library(it)) }
        dependencies.add("implementation", project(":core:designsystem"))
        dependencies.add("implementation", project(":domain:base"))
        dependencies.add("implementation", project(":presentation:base"))
        dependencies.add("implementation", project(":core:navigation"))
    }
}
