package convention.base

import com.android.build.api.dsl.CommonExtension
import convention.config.library
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        (extensions.getByName("android") as CommonExtension).buildFeatures.compose = true
        dependencies.add("implementation", dependencies.platform(library("compose-bom")))
        dependencies.add("implementation", library("compose-ui"))
        dependencies.add("implementation", library("compose-foundation"))
        dependencies.add("implementation", library("compose-material3"))
        dependencies.add("implementation", library("compose-ui-tooling-preview"))
        dependencies.add("debugImplementation", library("compose-ui-tooling"))
    }
}
