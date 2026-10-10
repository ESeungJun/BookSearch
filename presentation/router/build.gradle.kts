plugins {
    id("convention.library")
    alias(libs.plugins.kotlin.serialization) // PageData 를 백스택에 저장·복원
}

// 화면마다 Router(넘길 값 PageData + 여는 함수)를 둔다. 다른 기능 화면으로 갈 때는 이 모듈의 Router 만 쓰고,
// 구현(RouterImpl)은 그 화면의 main 모듈이 Hilt 로 바인딩한다. 그래서 기능 main 모듈끼리는 서로를 모른다.
dependencies {
    implementation(libs.nav3.runtime)
    implementation(libs.kotlinx.serialization.json)
}
