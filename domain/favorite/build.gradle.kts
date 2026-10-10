plugins {
    id("convention.kotlin.jvm")
}

dependencies {
    // 공개 함수가 BookDTO·BookException 을 주고받으므로, 이 모듈을 쓰는 쪽도 :domain:base 을 볼 수 있게 api 로 둔다
    api(projects.domain.base)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
