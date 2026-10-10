plugins {
    id("convention.di")
}

dependencies {
    implementation(projects.domain.favorite)
    implementation(projects.core.database) // 데이터 소스 생성자의 DAO 타입
    implementation(projects.data.favorite)
}
