import org.gradle.api.Plugin
import org.gradle.api.Project

/** domain 레이어(:domain:<기능>) 공통 구성. 순수 Kotlin 이고, 공통 타입·결과 처리는 :domain:base 에서 가져온다. */
class DomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.kotlin.jvm")
        dependencies.add("implementation", library("kotlinx-coroutines-core"))
        dependencies.add("implementation", library("javax-inject")) // UseCase 생성자 주입(@Inject). Hilt 없이 표준 어노테이션만 쓴다
        dependencies.add("implementation", project(":domain:base"))
    }
}
