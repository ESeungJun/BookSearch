import java.util.Properties

plugins {
    id("convention.library")
    id("convention.hilt")
}

// local.properties 의 키를 BuildConfig 로 넣는다. 파일이나 키가 없어도 빌드는 통과한다 —
// 그때는 요청이 401 로 실패하고 화면에는 공통 오류 문구가 나온다. 키를 아는 모듈은 이 모듈 하나다.
val apiKey: String = rootProject.file("local.properties").takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use(::load) }.getProperty("KAKAO_REST_API_KEY") }
    .orEmpty()

android {
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField("String", "KAKAO_REST_API_KEY", "\"$apiKey\"")
    }
}

// OkHttp·Retrofit 제공(앱에 하나). API 서비스는 그것을 쓰는 data 모듈이 이 Retrofit 으로 만든다.
dependencies {
    implementation(libs.bundles.network)
}
