plugins {
    id("convention.presentation")
}

dependencies {
    implementation(projects.presentation.favorite.route)
    implementation(projects.presentation.detail.route)
}
