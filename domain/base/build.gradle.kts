plugins {
    id("convention.kotlin.jvm")
}

// domain 공통: 여러 기능이 함께 쓰는 타입(DTO·DomainResult)과 UseCase 결과 처리.
dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
