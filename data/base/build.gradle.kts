plugins {
    id("convention.kotlin.jvm")
}

// 원격 데이터 소스가 함께 쓰는 공통 코드. Android 가 필요 없어 순수 Kotlin 모듈이다.
dependencies {
    api(projects.domain.base) // apiCall 이 DomainResult 를 돌려준다
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit)
}
