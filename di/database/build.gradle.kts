plugins {
    id("convention.library")
    id("convention.hilt")
}

// Room DB 와 DAO 제공. DB 는 세 기능(검색·즐겨찾기·상세)이 함께 쓰므로 앱에 하나만 만든다.
dependencies {
    implementation(projects.data.database)
}
