// 컨벤션 플러그인: 모듈들이 똑같이 반복하던 빌드 설정을 플러그인으로 묶는다.
// - 모듈마다 각자 SDK·Java 버전·Compose·Hilt·테스트 의존·레이어 공통 의존을 적으면, 값 하나를 바꿀 때 모든 파일을 고쳐야 하고
//   모듈마다 설정이 조금씩 달라지기 쉽다. 플러그인으로 두면 바꿀 곳이 한 파일이고, 모듈의 build.gradle.kts 에는
//   레이어 플러그인 한 줄과 그 모듈만 쓰는 의존만 남는다. 새 기능 모듈도 플러그인만 적용하면 같은 규칙을 따른다.
// - 루트의 allprojects {}·subprojects {} 로 일괄 설정하지 않는다. 어떤 모듈에 무엇이 적용되는지 모듈 파일만 봐서는 알 수 없고,
//   모듈끼리 설정이 묶여 Gradle 의 프로젝트별 구성 격리와 맞지 않는다. 플러그인은 각 모듈이 id 로 직접 적용한다.
// - buildSrc 대신 포함 빌드(settings 의 includeBuild)로 둔다. buildSrc 는 내용이 바뀌면 모든 빌드 스크립트가 다시 컴파일되지만,
//   포함 빌드는 일반 Gradle 빌드처럼 따로 컴파일·캐시되고 버전 카탈로그도 같은 파일을 쓴다.
//
// 패키지: config(SDK·카탈로그 접근·Android 공통 설정) / base(Android·Kotlin·Compose·Hilt 기반 플러그인) / layer(레이어별 조합)

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("application") { id = "convention.application"; implementationClass = "convention.base.ApplicationConventionPlugin" }
        register("library") { id = "convention.library"; implementationClass = "convention.base.LibraryConventionPlugin" }
        register("kotlinJvm") { id = "convention.kotlin.jvm"; implementationClass = "convention.base.KotlinJvmConventionPlugin" }
        register("compose") { id = "convention.compose"; implementationClass = "convention.base.ComposeConventionPlugin" }
        register("hilt") { id = "convention.hilt"; implementationClass = "convention.base.HiltConventionPlugin" }
        register("presentation") { id = "convention.presentation"; implementationClass = "convention.layer.PresentationConventionPlugin" }
        register("domain") { id = "convention.domain"; implementationClass = "convention.layer.DomainConventionPlugin" }
        register("data") { id = "convention.data"; implementationClass = "convention.layer.DataConventionPlugin" }
    }
}
