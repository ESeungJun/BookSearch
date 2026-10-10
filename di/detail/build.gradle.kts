plugins {
    id("convention.di")
}

dependencies {
    implementation(projects.domain.detail)
    implementation(projects.data.detail)
    implementation(projects.core.database) // 데이터 소스 생성자의 DAO 타입
}
