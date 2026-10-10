import org.gradle.api.Plugin
import org.gradle.api.Project

/** presentation 레이어(:presentation:<기능>) 공통 구성. 화면은 :core:designsystem·:domain:base 와 각 모듈이 적은 :domain:<기능> 만 안다. */
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
        ).forEach { dependencies.add("implementation", library(it)) }
        dependencies.add("implementation", project(":core:designsystem"))
        dependencies.add("implementation", project(":domain:base"))
    }
}
