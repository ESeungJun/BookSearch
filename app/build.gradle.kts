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
    // Hilt 는 :app 에서 그래프를 만든다. Hilt 모듈을 가진 모듈(data·core)을 :app 이 모두 알아야 한다
    implementation(projects.core.network)
    implementation(projects.data.base)
    implementation(projects.data.api.searchbook)
    implementation(projects.data.local.favorite)
    implementation(projects.data.local.book)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.navigation) // 탭·백스택·넓은 창의 목록-상세 2칸
}
