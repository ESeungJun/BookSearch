package convention.config

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** 모든 Android 모듈 공통 설정. 네임스페이스는 모듈 경로 그대로다(:presentation:search → presentation.search). 패키지만 보고 어느 모듈인지 알 수 있게 한다. */
fun Project.configureAndroid(extension: CommonExtension) {
    extension.apply {
        namespace = path.removePrefix(":").replace(':', '.')
        compileSdk = ProjectConfig.COMPILE_SDK
        defaultConfig.minSdk = ProjectConfig.MIN_SDK
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }
    // AGP 9 는 Kotlin 을 내장한다 — kotlin.android 플러그인 없이 확장만 설정한다
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    addTestDependencies()
}
