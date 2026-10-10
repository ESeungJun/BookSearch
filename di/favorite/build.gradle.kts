plugins {
    id("convention.library")
    id("convention.hilt")
}

dependencies {
    implementation(projects.domain.favorite)
    implementation(projects.data.database)
    implementation(projects.data.favorite)
}
