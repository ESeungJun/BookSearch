plugins {
    id("convention.library")
}

// 기능 모듈과 :app 이 함께 쓰는 화면 이동 계약. 기능 모듈은 서로의 화면을 모르고, 이동은 경로(route 모듈)와 INavigator 로만 한다.
dependencies {
    implementation(libs.nav3.runtime)
}
