import org.gradle.api.Plugin
import org.gradle.api.Project

/** di 레이어(:di:<이름>) 공통 구성. Hilt 모듈만 둔다. */
class DiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("convention.hilt")
    }
}
