import java.util.Properties

plugins {
    id("convention.library")
    id("convention.hilt")
}

// local.properties 의 키를 BuildConfig 로 넣는다. 파일이나 키가 없어도 빌드는 통과한다("별도 설정 없이 빌드" 조건) —
// 그때는 요청이 401 로 실패하고 화면에 키 확인 안내가 나온다. 키를 아는 모듈은 이 모듈 하나다.
val apiKey: String = rootProject.file("local.properties").takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use(::load) }.getProperty("KAKAO_REST_API_KEY") }
    .orEmpty()

android {
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField("String", "KAKAO_REST_API_KEY", "\"$apiKey\"")
    }
}

// presentation 과 data 를 잇는 유일한 모듈. :data 를 의존하는 곳은 여기뿐이라
// 화면 모듈은 구현체(Retrofit·Room 타입)를 컴파일 단계에서 볼 수 없다.
dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.room.runtime)
}
