plugins {
    id("convention.library")
    id("convention.hilt")
}

// 책 기능의 바인딩과 Room DB. DB 는 세 기능이 함께 쓰므로 공통인 책 모듈에서 하나만 만든다.
dependencies {
    implementation(projects.domain.book)
    implementation(projects.data.book)
}
