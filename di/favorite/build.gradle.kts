plugins {
    id("convention.di")
}

dependencies {
    implementation(projects.domain.favorite)
    implementation(projects.core.database)
    implementation(projects.data.favorite)
}
