plugins {
    id("convention.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject) // UseCase 생성자 주입(@Inject). Hilt 없이 표준 어노테이션만 쓴다
}
