plugins {
    id("convention.presentation")
}

dependencies {
    implementation(projects.domain.book)
    implementation(projects.domain.favorite)
    // 전체 책 소개(도서 페이지)를 앱을 떠나지 않고 Custom Tab 으로 연다
    implementation(libs.androidx.browser)
    implementation(libs.coil.compose) // 큰 표지 이미지. URL 로더는 앱에 함께 들어간 coil-network-okhttp 가 맡는다
}
