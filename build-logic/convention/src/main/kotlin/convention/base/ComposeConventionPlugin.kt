package convention.base

import com.android.build.api.dsl.CommonExtension
import convention.config.bundle
import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        (extensions.getByName("android") as CommonExtension).buildFeatures.compose = true
        dependencies.add("implementation", dependencies.platform(library("compose-bom")))
        dependencies.add("implementation", bundle("compose"))
        dependencies.add("debugImplementation", library("compose-ui-tooling"))
    }
}
