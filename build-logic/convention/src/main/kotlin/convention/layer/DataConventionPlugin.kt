package convention.layer

import convention.config.bundle
import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * data 레이어(:data:<기능>) 공통 구성. 공통 기반(:data:base — 결과 변환·Room DB)과 domain 공통 타입을 가져온다.
 * 기능의 Hilt 바인딩은 그 기능 data 모듈의 di 패키지에 두므로 Hilt 도 붙인다.
 */
class DataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.library")
        pluginManager.apply("convention.hilt")
        dependencies.add("implementation", bundle("layer-common")) // 코루틴, UseCase·구현 생성자 주입(@Inject)
        dependencies.add("implementation", project(":domain:base"))
        dependencies.add("implementation", project(":data:base"))
    }
}
