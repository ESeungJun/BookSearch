# 구조 개요

이 문서는 앱 전체의 큰 그림을 설명합니다. 레이어별 자세한 설명은 각 폴더의 README 에 있습니다.

| 레이어 | 문서 |
|---|---|
| 앱 골격 | [`app/README.md`](app/README.md) |
| 공통 기반 | [`core/README.md`](core/README.md) |
| 화면 | [`presentation/README.md`](presentation/README.md) |
| 도메인 | [`domain/README.md`](domain/README.md) |
| 데이터 | [`data/README.md`](data/README.md) |

코드 작성 규칙(이름, 순서, 주석, 테스트, 커밋)은 [`CLAUDE.md`](CLAUDE.md), 각 결정의 이유는 [`docs/ai/decision-log.md`](docs/ai/decision-log.md) 에 있습니다.

---

## 1. 설계 원칙

1. **레이어 × 기능으로 나눈다.** 레이어(presentation · domain · data)를 먼저 나누고, 그 안을 기능(검색 · 즐겨찾기 · 책)으로 나눕니다. 여러 사람이 서로 다른 기능을 동시에 고쳐도 모듈이 겹치지 않게 하려는 것입니다.
2. **의존은 한 방향이다.** `presentation → domain ← data`. domain 은 아무것도 모르는 순수 Kotlin 이고, presentation 과 data 는 서로를 모릅니다.
3. **기능끼리 직접 의존하지 않는다.** 다른 기능의 화면으로 갈 때는 Router 계약만 씁니다. 기능 하나를 고치거나 더해도 다른 기능 모듈은 그대로입니다.
4. **설명할 수 있는 만큼만 만든다.** 쓰임이 하나뿐인 추상화나 미리 만든 공용 코드는 두지 않습니다. 공통 모듈에는 두 곳 이상이 실제로 쓰는 것만 둡니다.

## 2. 모듈 지도 (17개)

```
                          :app
        (Activity · 탭 골격 · NavDisplay · Hilt 그래프)
          │                │                    │
          ▼                ▼                    ▼
 :presentation:feature:*:main   :presentation:router   :data:api:* · :data:local:*
   (검색 · 즐겨찾기 · 상세 화면)     (화면 이동 계약)        (저장소 구현)
          │                                     │
          ├──────────► :domain:* ◄───────────────┤
          │        (UseCase · 저장소 인터페이스)     │
          ▼                                     ▼
 :presentation:base                         :data:base
 (책 카드 · 표시 규칙)                        (결과 변환 · Room DB)

 :core:designsystem · :core:navigation · :core:network   ← 레이어와 무관한 기반
```

| 모듈 | 하는 일 |
|---|---|
| `:app` | 유일한 Activity, 탭 골격(하단 탭바 · 측면 레일), 탭별 백스택, 화면 등록을 모아 `NavDisplay` 로 그림, Hilt 그래프 |
| `:core:designsystem` | 테마, 간격 토큰, 두 화면 이상이 쓰는 UI(검색창, 접히는 머리, 상태 화면 등) |
| `:core:navigation` | `INavigator`(이동) · `EntryProviderInstaller`(화면 등록) 계약 |
| `:core:network` | OkHttp · Retrofit 설정, API 키 |
| `:presentation:base` | 책 카드, 하트 버튼, 카드 표시 값 변환, 공통 문구 |
| `:presentation:router` | 화면별 `XxxRouter`(넘길 값 `PageData` + 여는 함수) |
| `:presentation:feature:{search,favorite,detail}:main` | 화면 하나씩: ViewModel, Compose 화면, 화면 상태 |
| `:domain:base` | `BookDTO`, `DomainResult` |
| `:domain:{search,favorite,book}` | 주제별 UseCase 와 저장소 인터페이스 |
| `:data:base` | 결과 변환(`safeApiCall` · `safeDbCall` · `safeDbFlow`), Room DB(테이블 · DAO), Entity ↔ DTO |
| `:data:api:searchbook` | 카카오 검색 API 호출, 검색 결과 캐시, 오프라인 대체 |
| `:data:local:{favorite,book}` | 기기 DB 만 쓰는 즐겨찾기 · 책 한 권 조회 |

모듈 경로는 곧 패키지 이름입니다(`:data:api:searchbook` → `data.api.searchbook`). `:app` 만 applicationId(`com.leeseungjun.booksearch`)를 씁니다.

## 3. 데이터가 흐르는 길 — 검색 한 번

```
SearchScreen ──입력──► SearchViewModel ──► SearchBooksUseCase ──► ISearchRepository
   ▲                       │ (400ms 디바운스)                          │ (구현: SearchRepositoryImpl)
   │                       ▼                                          ▼
   └──── SearchUiState ◄── when(DomainResult) ◄──── SearchPageDTO ◄── 원격 3초 → 실패하면 로컬
                                                                   │            │
                                                     SearchRemoteDataSource   SearchLocalDataSource
                                                     (SearchBookApi)          (BookEntity · SearchCacheEntity)
```

1. 화면은 입력을 ViewModel 에 넘기고, ViewModel 은 입력이 400ms 멈추면 UseCase 를 부릅니다.
2. UseCase 는 저장소 함수 하나를 부르고 결과를 그대로 돌려줍니다.
3. 저장소는 서버를 먼저 부릅니다. 3초를 넘기거나 실패하면 저장해 둔 결과를 대신 돌려줍니다. 성공하면 결과를 저장합니다.
4. 데이터 소스는 서버 응답(`~Api`)이나 DB 행(`~Entity`)을 그대로 주고, 저장소가 그것을 DTO 로 바꿉니다.
5. 결과는 `DomainResult` 로 올라옵니다. ViewModel 이 `when` 으로 성공 · 실패 · 에러를 모두 판단해 화면 상태(`UiState`) 하나를 바꾸고, 화면은 그 상태를 그립니다.

### 결과 타입 `DomainResult`

| 경우 | 뜻 | 만드는 곳 |
|---|---|---|
| `Success(data)` | 성공(저장해 둔 결과로 대신한 경우도 포함) | data |
| `Fail(code)` | 서버가 응답했지만 성공이 아님(HTTP 코드만 담음) | `safeApiCall` |
| `Error(cause)` | 연결 실패, 시간 초과, 형식 오류, DB 오류 | `safeApiCall` · `safeDbCall` · 저장소 |

domain 은 상황만 전하고 원인을 나누지 않습니다. 사용자에게 어떻게 보일지는 화면이 정합니다(지금은 원인과 관계없이 문구 하나).

## 4. 화면 이동

- 탭마다 백스택을 따로 둡니다(`rememberNavBackStack`). 모든 탭의 항목을 한 `NavDisplay` 에 넘겨, 탭을 바꿔도 다른 탭의 화면 상태와 ViewModel 이 남습니다.
- 다른 기능 화면으로 갈 때는 `:presentation:router` 의 Router 를 주입받아 `open(PageData)` 를 부릅니다. Router 구현은 그 화면을 가진 모듈이 `INavigator` 로 만들고 Hilt 로 바인딩합니다.
- 각 화면 모듈은 "PageData → 화면" 연결을 Hilt `@IntoSet` 으로 내놓고, `:app` 은 그 Set 하나만 받아 등록합니다. 그래서 `:app` 은 기능 화면을 하나하나 알지 않습니다.
- 창 너비가 600dp 이상이면 목록과 상세를 한 화면에 나란히 둡니다(`ListDetailSceneStrategy`).
- 뒤로 가기는 지금 탭 안에서만 움직입니다. 2칸이면 상세만 닫고, 즐겨찾기 탭 첫 화면에서는 검색 탭으로, 검색 탭 첫 화면에서는 시스템에 맡깁니다(`CurrentTabSceneStrategy`).

## 5. 의존성 주입 (Hilt)

| 범위 | 무엇 | 이유 |
|---|---|---|
| `SingletonComponent` | Room DB, OkHttp, Retrofit | 앱에 하나여야 동작한다(DB 변경 감지, 연결 풀) |
| `ActivityRetainedComponent` | `AppNavigator`, Router 구현, 화면 등록 | 탭 백스택을 가리키는 이동 담당이 화면 회전에도 하나로 유지되어야 한다 |
| `ViewModelComponent` | 저장소, 데이터 소스, 검색 API 서비스 | 상태가 없어 앱 전체에 하나일 필요가 없다. 화면끼리 같은 데이터를 보는 것은 DB 의 Flow 가 맡는다 |

바인딩은 구현이 있는 모듈의 `di/` 패키지에 둡니다. 따로 모은 di 모듈은 없습니다.

## 6. 오프라인과 저장소

- 테이블은 셋입니다: `book`(책 정보, 한 권에 한 행) · `favorite`(키와 저장 시각) · `search_cache`(검색어 · 정렬 · 페이지별 순서).
- 같은 책이 여러 검색 · 즐겨찾기에 나와도 책 정보는 `book` 에 한 번만 저장됩니다. 상세 화면은 이 테이블에서 키로 읽습니다.
- 검색 캐시는 (검색어 · 정렬) 최근 20조합 × 5페이지를 남기고 만료는 없습니다. 같은 검색의 저장 결과가 없으면 저장된 모든 책에서 검색어로 찾습니다.

## 7. 빌드 구성

- 모듈 공통 빌드 설정은 `build-logic`(포함 빌드)의 컨벤션 플러그인에 있습니다. 모듈의 `build.gradle.kts` 에는 레이어 플러그인 한 줄과 그 모듈만 쓰는 의존만 적습니다.

| 플러그인 | 붙이는 것 |
|---|---|
| `convention.application` · `convention.library` · `convention.kotlin.jvm` | SDK · Java 버전 · 테스트 의존 |
| `convention.compose` · `convention.hilt` | Compose, Hilt(KSP) |
| `convention.presentation` | 화면 모듈 공통(Compose · Hilt · `:presentation:base` · `:presentation:router` 등) |
| `convention.domain` | 순수 Kotlin, `:domain:base`, UseCase 팩토리 생성(dagger-compiler) |
| `convention.data` | Hilt, `:domain:base`, `:data:base` |

- domain 모듈에 dagger-compiler 를 붙이는 이유: UseCase 의 팩토리 클래스를 domain 에서 한 번만 만들기 위해서입니다. 없으면 같은 UseCase 를 쓰는 화면 모듈마다 같은 팩토리를 만들어, 앱 빌드의 dex 병합이 중복 클래스로 실패합니다.
- 버전은 `gradle/libs.versions.toml` 한곳에 있습니다.
