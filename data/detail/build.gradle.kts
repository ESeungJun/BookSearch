plugins {
    id("convention.library")
}

dependencies {
    implementation(projects.domain.detail)
    implementation(projects.data.database)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
