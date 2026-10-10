package convention.config

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** 플러그인 코드에서 gradle/libs.versions.toml 을 읽는다. 버전은 카탈로그 한곳에만 둔다. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.library(alias: String) = libs.findLibrary(alias).get()

fun Project.bundle(alias: String) = libs.findBundle(alias).get()

fun Project.addTestDependencies() {
    dependencies.add("testImplementation", bundle("test-unit"))
}
