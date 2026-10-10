import java.util.Properties

plugins {
    id("convention.di")
}

// local.properties 의 키를 BuildConfig 로 넣는다. 파일이나 키가 없어도 빌드는 통과한다 —
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

// OkHttp·Retrofit 과 API 서비스 제공. 기능 모듈(:di:<기능>)은 서비스를 주입받기만 한다.
dependencies {
    implementation(projects.data.search) // ISearchBookService
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}
