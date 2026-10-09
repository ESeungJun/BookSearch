plugins {
    id("convention.library")
    id("convention.hilt")
}

// presentation 과 data 를 잇는 유일한 모듈. :data 를 의존하는 곳은 여기뿐이라
// 화면 모듈은 구현체(Retrofit·Room 타입)를 컴파일 단계에서 볼 수 없다.
dependencies {
    implementation(projects.domain)
    implementation(projects.data)
}
