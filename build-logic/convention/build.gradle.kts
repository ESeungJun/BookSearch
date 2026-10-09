plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("application") { id = "convention.application"; implementationClass = "ApplicationConventionPlugin" }
        register("library") { id = "convention.library"; implementationClass = "LibraryConventionPlugin" }
        register("kotlinJvm") { id = "convention.kotlin.jvm"; implementationClass = "KotlinJvmConventionPlugin" }
        register("compose") { id = "convention.compose"; implementationClass = "ComposeConventionPlugin" }
        register("hilt") { id = "convention.hilt"; implementationClass = "HiltConventionPlugin" }
        register("feature") { id = "convention.feature"; implementationClass = "FeatureConventionPlugin" }
    }
}
