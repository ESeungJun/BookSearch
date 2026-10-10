plugins {
    id("convention.library")
    id("convention.hilt")
}

// 검색 기능의 바인딩. 서비스·네트워크는 :di:network 가 제공한다.
// presentation 은 :di:* 를 보지 않으므로 화면 모듈은 Retrofit·Room 타입을 컴파일 단계에서 볼 수 없다.
dependencies {
    implementation(projects.domain.search)
    implementation(projects.data.search)
}
