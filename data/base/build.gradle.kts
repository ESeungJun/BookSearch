plugins {
    id("convention.library")
    id("convention.hilt") // DB·DAO 제공(di/DatabaseModule)
    alias(libs.plugins.ksp) // Room 코드 생성
}

// data 레이어 공통 기반: 서버·DB 호출 결과 변환(safeApiCall·safeDbCall), 로컬 DB(db/), Entity ↔ DTO 변환.
// Room DB 는 모든 테이블 클래스를 한곳에서 알아야 해서 DB·DAO·Entity 를 이 모듈에 모은다. 기능 data 모듈은 서로를 참조하지 않고 이 모듈만 본다.
dependencies {
    implementation(projects.domain.base)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.serialization.json) // 저자 목록 TypeConverter

    // DAO 쿼리를 메모리 DB 로 JVM 에서 확인한다(Robolectric 이 Android SQLite 를 제공)
    testImplementation(libs.bundles.test.android)
}
