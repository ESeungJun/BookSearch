plugins {
    id("convention.application")
    id("convention.compose")
    id("convention.hilt")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    // 화면 연결·Router 구현은 각 기능 main 모듈이 Hilt 로 내놓는다. :app 은 탭 시작 PageData 만 직접 쓴다
    implementation(projects.presentation.feature.search.main)
    implementation(projects.presentation.feature.favorite.main)
    implementation(projects.presentation.feature.detail.main)
    implementation(projects.presentation.router)
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
}
