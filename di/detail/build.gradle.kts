plugins {
    id("convention.library")
    id("convention.hilt")
}

dependencies {
    implementation(projects.domain.detail)
    implementation(projects.data.detail)
}
