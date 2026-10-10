plugins {
    id("convention.library")
}

// data 공통: 서버 호출 결과 변환(apiCall), Entity ↔ DTO 변환.
dependencies {
    implementation(projects.domain.base)
    implementation(projects.core.database)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit)
}
