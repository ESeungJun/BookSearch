plugins {
    id("convention.presentation")
}

dependencies {
    implementation(projects.presentation.search.route)
    implementation(projects.presentation.detail.route)
}
