plugins {
    id("convention.di")
}

dependencies {
    implementation(projects.domain.detail)
    implementation(projects.data.detail)
}
