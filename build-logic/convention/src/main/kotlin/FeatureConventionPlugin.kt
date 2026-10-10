import org.gradle.api.Plugin
import org.gradle.api.Project

/** 화면 모듈(:presentation:<기능>) 공통 구성. 화면은 디자인 시스템과, 각 모듈이 build.gradle.kts 에 적은 :domain:<기능> 만 안다. */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("convention.compose")
        pluginManager.apply("convention.hilt")
        listOf(
            "androidx-lifecycle-runtime-compose",
            "androidx-lifecycle-viewmodel-compose",
            "hilt-lifecycle-viewmodel-compose",
            "kotlinx-collections-immutable",
        ).forEach { dependencies.add("implementation", library(it)) }
        dependencies.add("implementation", project(":presentation:designsystem"))
    }
}
