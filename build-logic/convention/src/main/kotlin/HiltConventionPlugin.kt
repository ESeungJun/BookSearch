import org.gradle.api.Plugin
import org.gradle.api.Project

/** Hilt 는 kapt 대신 KSP 로 처리한다 — Kotlin 2.x 에서 kapt 는 유지보수 모드이고 빌드가 느리다. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        dependencies.add("implementation", library("hilt-android"))
        dependencies.add("ksp", library("hilt-compiler"))
    }
}
