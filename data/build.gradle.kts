plugins {
    id("convention.library")
    alias(libs.plugins.ksp) // Room 코드 생성
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.domain.book)
    implementation(projects.domain.search)
    implementation(projects.domain.favorite)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.javax.inject)
}
