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
    // Hilt 는 :app 에서 그래프를 만든다. Hilt 모듈을 가진 모듈(data 기능·core)을 :app 이 모두 알아야 한다
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.data.search)
    implementation(projects.data.favorite)
    implementation(projects.data.detail)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.compose.navigation.suite)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
}
