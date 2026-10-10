pluginManagement {
    // 컨벤션 플러그인은 buildSrc 대신 포함 빌드 — 플러그인 파일 하나를 고쳐도 전 모듈이 다시 구성되지 않는다
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // gradle-daemon-jvm.properties 의 JDK 17 이 없으면 내려받는다
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "BookSearch"

include(":app")
include(":di:network", ":di:database", ":di:search", ":di:favorite", ":di:detail")
include(":core:designsystem")
include(":presentation:search", ":presentation:favorite", ":presentation:detail")
include(":domain:base", ":domain:search", ":domain:favorite", ":domain:detail")
include(":data:database", ":data:search", ":data:favorite", ":data:detail")
