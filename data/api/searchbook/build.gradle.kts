plugins {
    id("convention.data")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.domain.search)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
}
