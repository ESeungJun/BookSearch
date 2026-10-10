pluginManagement {
    // 모듈 공통 빌드 설정(컨벤션 플러그인)을 담은 포함 빌드
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
include(":core:designsystem", ":core:database", ":core:network", ":core:navigation")
include(":presentation:base")
include(":presentation:router")
include(":presentation:search:main", ":presentation:favorite:main", ":presentation:detail:main")
include(":domain:base", ":domain:search", ":domain:favorite", ":domain:detail")
include(":data:base", ":data:search", ":data:favorite", ":data:detail")
