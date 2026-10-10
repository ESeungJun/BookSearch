plugins {
    id("convention.library")
    id("convention.compose")
}

// 기능과 무관한 앱 공통 코드: designsystem(테마·두 화면 이상이 쓰는 UI), util(공통 유틸).
// 기능 모듈은 이 모듈만 공통으로 보고 서로는 모른다. 한 기능만 쓰는 것은 여기 두지 않는다.
