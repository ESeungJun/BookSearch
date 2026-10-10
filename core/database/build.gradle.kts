plugins {
    id("convention.library")
    id("convention.hilt") // DB·DAO 제공(di/DatabaseModule)
    alias(libs.plugins.ksp) // Room 코드 생성
}

// Room DB 는 모든 테이블 클래스를 한곳에서 알아야 해서 DB·DAO·Entity 를 이 모듈에 모은다.
// 기능 data 모듈이 서로를 참조하지 않고 이 모듈만 보도록 core 에 둔다. domain 타입은 모른다(Entity ↔ DTO 변환은 :data:base).
dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json) // 저자 목록 TypeConverter
}
