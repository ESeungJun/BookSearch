import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Hilt 는 kapt 대신 KSP 로 처리한다 — Kotlin 2.x 에서 kapt 는 유지보수 모드이고 빌드가 느리다. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        dependencies.add("implementation", library("hilt-android"))
        dependencies.add("ksp", library("hilt-compiler"))
        // Dagger 는 기본으로 실제로 주입을 요청받은 바인딩만 검사한다. 모듈에 적은 바인딩을 모두 빌드 때 검사해
        // 아직 쓰는 화면이 없는 바인딩의 누락·오타도 실행 전에 드러나게 한다.
        extensions.configure<KspExtension> {
            arg("dagger.fullBindingGraphValidation", "ERROR")
        }
    }
}
