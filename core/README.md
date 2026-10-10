# :core — 레이어와 무관한 기반

어느 레이어의 일도 아니고 앱 데이터의 내용을 모르는 기반을 둡니다. 두 곳 이상이 실제로 쓰는 것만 둡니다.

| 모듈 | 하는 일 | 누가 쓰나 |
|---|---|---|
| `:core:designsystem` | 테마, 간격 토큰, 공통 UI | 화면 모듈 |
| `:core:navigation` | 화면 이동 계약 | 화면 모듈, `:app` |
| `:core:network` | OkHttp · Retrofit, API 키 | `:app`(Hilt 그래프) → data 모듈이 주입받음 |

## :core:designsystem

| 파일 | 내용 |
|---|---|
| `Theme` | `BookSearchTheme`(Material 3, 시스템 다크 모드를 따름) |
| `theme/Spacing` | 간격 토큰 8 · 12 · 16 · 32dp. 화면의 여백은 이 값만 쓴다 |
| `component/SearchField` | 검색 입력창(지우기 버튼 포함) |
| `component/CollapsingHeader` | 스크롤하면 접히는 머리. 손가락 방향을 머리가 먼저 받고, 고정 줄은 남긴다 |
| `component/DropdownSelector` | 정렬 · 필터 선택 버튼 + 메뉴 |
| `component/ScrollToTopButton` | 맨 위로 버튼 |
| `component/StateViews` | 빈 화면 · 오류 화면(다시 시도) |
| `component/SkeletonBlock` | 로딩 골격의 한 칸 |

책이나 즐겨찾기처럼 domain 타입을 아는 UI 는 여기가 아니라 `:presentation:base` 에 둡니다.

## :core:navigation

| 타입 | 내용 |
|---|---|
| `INavigator` | `navigate` · `replace` · `back` · `current`. 구현은 `:app` 의 `AppNavigator` |
| `EntryProviderInstaller` | 기능 모듈이 "PageData → 화면" 연결을 등록하는 함수 타입. `:app` 이 Set 으로 모은다 |
| `LocalCurrentRoute` | 지금 탭에서 맨 위에 보이는 화면의 경로(CompositionLocal). 2칸에서 목록이 상세에 열린 책을 표시하는 데 쓴다 |

## :core:network

- `NetworkModule` 이 OkHttp · Retrofit 을 싱글톤으로 제공합니다(연결 풀을 앱 전체가 함께 씀).
- API 키는 `local.properties` 의 `KAKAO_REST_API_KEY` 를 빌드 때 `BuildConfig` 로 넣습니다. 키를 아는 모듈은 이 모듈 하나입니다.
- 모든 요청에 `Authorization: KakaoAK <키>` 헤더를 붙입니다.
- 통신 로그는 debug 빌드에만 남기고(JSON 을 줄 단위로 들여 써서 출력), 인증 헤더는 가립니다. release 는 남기지 않습니다.
