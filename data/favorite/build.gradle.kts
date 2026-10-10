plugins {
    id("convention.library")
}

dependencies {
    implementation(projects.domain.favorite)
    implementation(projects.data.book)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
