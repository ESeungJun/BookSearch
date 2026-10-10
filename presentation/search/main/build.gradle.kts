plugins {
    id("convention.presentation")
}

dependencies {
    implementation(projects.presentation.search.route)
    implementation(projects.presentation.detail.route)
    implementation(projects.domain.search)
    implementation(projects.domain.favorite)
    implementation(libs.compose.material.icons.extended)
}
