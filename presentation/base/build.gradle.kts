plugins {
    id("convention.library")
    id("convention.compose")
}

// 여러 기능 화면이 함께 쓰는 UI 중 domain 타입(BookDTO)을 아는 것(책 카드·카드 표시 값 변환)과 공통 문구(오류·숫자).
// domain 을 모르는 범용 UI 는 :core:designsystem 에 둔다. 기능 화면 모듈은 convention.presentation 이 이 모듈을 붙인다.
dependencies {
    implementation(projects.domain.base)
    implementation(projects.core.designsystem) // 간격 토큰
    // Coil 3 은 네트워크 로더를 따로 붙여야 URL 이미지를 받는다(image 묶음에 함께 있다)
    implementation(libs.bundles.image)
}
