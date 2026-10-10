plugins {
    id("convention.library")
    alias(libs.plugins.ksp) // Room 코드 생성
}

// Room DB 는 모든 테이블 클래스를 한곳에서 알아야 해서 DB·DAO·Entity 는 이 모듈에 모은다.
// 테이블을 읽고 쓰는 로직은 각 기능 모듈(:data:search·:data:favorite·:data:detail)의 로컬 데이터 소스에 있다.
dependencies {
    implementation(projects.domain.base)
    api(libs.room.runtime) // DAO 인터페이스의 Room 어노테이션을 기능 모듈도 볼 수 있어야 한다
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json) // 저자 목록 TypeConverter
    implementation(libs.javax.inject)
}
