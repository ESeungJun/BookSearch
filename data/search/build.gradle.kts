plugins {
    id("convention.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.domain.search)
    implementation(projects.data.database)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.javax.inject)
}
