package convention.layer

import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 기능의 route 모듈(:presentation:<기능>:route) 구성. 화면 이동에 쓰는 경로(NavKey)만 둔다.
 * 다른 기능은 이 작은 모듈만 의존해 이동하고, 화면 구현(main)은 모른다.
 */
class RouteConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization") // 경로를 백스택에 저장·복원
        dependencies.add("implementation", library("nav3-runtime"))
        dependencies.add("implementation", library("kotlinx-serialization-json"))
    }
}
