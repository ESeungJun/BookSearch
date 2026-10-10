plugins {
    id("convention.kotlin.jvm")
}

dependencies {
    api(projects.domain.base) // 공개 함수가 BookDTO 를 돌려준다
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
