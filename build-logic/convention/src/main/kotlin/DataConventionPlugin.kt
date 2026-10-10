import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * data 레이어(:data:<기능>) 공통 구성. 공통 코드(:data:base)와 Room(:core:database), domain 공통 타입을 가져온다.
 * 기능의 Hilt 바인딩은 그 기능 data 모듈의 di 패키지에 두므로 Hilt 도 붙인다.
 */
class DataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("convention.hilt")
        dependencies.add("implementation", library("kotlinx-coroutines-core"))
        dependencies.add("implementation", library("javax-inject"))
        dependencies.add("implementation", project(":domain:base"))
        dependencies.add("implementation", project(":data:base"))
        dependencies.add("implementation", project(":core:database"))
    }
}
