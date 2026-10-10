import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal object ProjectConfig {
    const val APPLICATION_ID = "com.leeseungjun.booksearch"
    const val COMPILE_SDK = 37 // 과제 지정
    const val TARGET_SDK = 37
    const val MIN_SDK = 26
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.library(alias: String) = libs.findLibrary(alias).get()

/** 모든 Android 모듈 공통 설정. 네임스페이스는 모듈 경로 그대로다(:presentation:search → presentation.search). 패키지만 보고 어느 모듈인지 알 수 있게 한다. */
internal fun Project.configureAndroid(extension: CommonExtension) {
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

internal fun Project.addTestDependencies() {
    dependencies.add("testImplementation", library("junit"))
    dependencies.add("testImplementation", library("kotlinx-coroutines-test"))
}
