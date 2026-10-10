plugins {
    id("convention.application")
    id("convention.compose")
    id("convention.hilt")
    alias(libs.plugins.kotlin.serialization) // 내비게이션 경로(NavKey) 직렬화
}

dependencies {
    // 화면 이동을 아는 곳은 :app 하나다. 화면 모듈끼리는 서로 모른다
    implementation(projects.core.designsystem)
    implementation(projects.presentation.search)
    implementation(projects.presentation.favorite)
    implementation(projects.presentation.detail)
    implementation(projects.di.network)
    implementation(projects.di.database)
    implementation(projects.di.search)
    implementation(projects.di.favorite)
    implementation(projects.di.detail)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.compose.navigation.suite)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
}
