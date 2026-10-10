plugins {
    id("convention.presentation")
}

dependencies {
    implementation(projects.domain.search)
    implementation(projects.domain.favorite)
    implementation(libs.compose.material.icons.extended)
}
