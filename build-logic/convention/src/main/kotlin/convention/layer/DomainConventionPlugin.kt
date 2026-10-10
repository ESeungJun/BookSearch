package convention.layer

import convention.config.bundle
import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

/** domain 레이어(:domain:<기능>) 공통 구성. 순수 Kotlin 이고, 공통 타입·결과 처리는 :domain:base 에서 가져온다. */
class DomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.kotlin.jvm")
        dependencies.add("implementation", bundle("layer-common")) // 코루틴, UseCase·구현 생성자 주입(@Inject)
        // UseCase 의 Dagger 팩토리를 이 모듈에서 한 번 만든다. 만들지 않으면 UseCase 를 주입받는 화면 모듈마다
        // 같은 팩토리 클래스를 따로 만들어, 두 화면이 같은 UseCase 를 쓰면 앱 빌드(dex 병합)가 중복 클래스로 실패한다
        pluginManager.apply("com.google.devtools.ksp")
        dependencies.add("implementation", library("dagger"))
        dependencies.add("ksp", library("dagger-compiler"))
        dependencies.add("implementation", project(":domain:base"))
    }
}
