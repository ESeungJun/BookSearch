# BookSearch — 작업 규칙

카카오 도서 검색 API로 만드는 Android 채용 과제다. Kotlin, Coroutine/Flow, Jetpack Compose를 쓴다.
요구사항 원문은 `docs/requirements/assignment.md`, API 실측은 `docs/api.md`에 있다.
이 저장소는 두 번째 버전이다. 첫 버전은 구조가 과해 폐기했다(이유는 `docs/ai/decision-log.md` 「v1 폐기」).

## 금지
- 저장소의 어떤 파일에도 채용 회사명을 쓰지 않는다. GitHub 공개 조건이다. 필요하면 "채용 과제"로 쓴다.
- API 키 값은 출력하지 않고 어떤 문서에도 적지 않는다. 키는 `local.properties`의 `KAKAO_REST_API_KEY`에서만 읽는다.
- 과제가 지정한 버전은 바꾸지 않는다: Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.4.0 / KSP 2.3.10 / JDK 17 / compileSdk 37.

## 단순하게 — v1 폐기에서 얻은 규칙
- 작성자가 한 줄씩 설명할 수 있는 코드만 넣는다. 쓰임이 하나뿐인 추상화, 미리 만든 공용 코드는 만들지 않는다.
- 공통 모듈(`:core:*`)에는 두 화면 이상이 실제로 쓰는 것만 둔다.
- 표준 라이브러리가 하는 일은 직접 만들지 않는다(예: 로그는 `HttpLoggingInterceptor`).
- 작업은 단계별로 한다. 단계를 시작하기 전에 추가할 파일 목록을 작성자에게 확인받고, 끝나면 diff를 보여 주고 병합한다.

## 구조

구조 설명은 문서로 나눠 둔다. 이 파일에는 지켜야 할 규칙만 둔다.

| 문서 | 내용 |
|---|---|
| `ARCHITECTURE.md` | 앱 전체: 설계 원칙, 모듈 지도, 데이터 흐름, 화면 이동, DI 범위, 빌드 구성 |
| `app/README.md` · `core/README.md` · `presentation/README.md` · `domain/README.md` · `data/README.md` | 레이어별 모듈·패키지·동작, 새로 만들 때 순서 |
| `docs/guide/기능-추가.md` | 새 기능을 더하는 순서와 확인 목록 |
| `.claude/agents/onboarding.md` · `.claude/agents/feature-dev.md` | 문답용 온보딩 에이전트, 기능 개발 에이전트 |

- **구조를 바꾸면 같은 커밋에서 `ARCHITECTURE.md` 와 해당 레이어 README 를 고친다.** 온보딩 에이전트가 이 문서를 근거로 답하므로 문서가 코드와 어긋나면 안 된다.

### 구조 규칙

- 의존 방향: `presentation → domain ← data`. 기능 data 모듈(`:data:api:*`·`:data:local:*`)과 `:core:network` 를 의존하는 곳은 `:app` 하나다(Hilt 가 `:app` 에서 그래프를 만든다). `:data:base` 는 data 모듈과 `:app` 이 의존한다. presentation 은 data 를 볼 수 없다.
- presentation 패키지: `vm`(ViewModel) / `view`(Screen + Content) / `view.component`(화면을 이루는 조각, 파일 하나에 컴포넌트 하나와 그 `@Preview`) / `data`(UiState·화면용 타입) / `mapper`(DTO → ViewData 같은 변환 확장 함수 — ViewModel 안에 두지 않는다). `:presentation:base` 도 같은 구성이다: `data`(BookViewData) / `mapper`(카드 표시 값·표시 규칙·숫자 문구) / `view`(ToastEffect) / `view.component`(BookCard·FavoriteIconButton).
- 기능 main 모듈끼리 서로 의존하지 않는다. 다른 기능 화면으로 갈 때는 `:presentation:router` 의 `XxxRouter` 를 주입받아 `open(PageData)` 를 부른다. 구현 `XxxRouterImpl` 은 그 화면의 main 모듈이 `INavigator` 로 만들고 Hilt 로 바인딩한다. PageData → 화면 연결도 각 main 모듈이 Hilt(`@IntoSet EntryProviderInstaller`)로 내놓고 `:app` 이 모아 그린다.
- 모듈 공통 빌드 설정은 `build-logic`(포함 빌드)의 컨벤션 플러그인에 둔다. 패키지: `convention.config`(SDK·카탈로그 접근·Android 공통 설정) / `convention.base`(application·library·kotlin.jvm·compose·hilt) / `convention.layer`(presentation·domain·data).
- 의존은 모두 `implementation` 으로 쓴다. `api` 로 다른 모듈을 내보내지 않는다. 레이어 공통 의존(코루틴·`javax.inject`·레이어 base 모듈)은 레이어 컨벤션 플러그인(`convention.domain`·`convention.data`·`convention.presentation`)이 붙이고, 모듈의 build.gradle.kts 에는 그 모듈만 쓰는 의존만 적는다.
- domain 저장소 인터페이스의 모든 함수는 `DomainResult`(관찰은 `Flow<DomainResult<T>>`)를 돌려준다.
- 결과의 공통 처리는 레이어마다 따로 둔다: data 는 `safeApiCall {}`(서버, 원격 데이터 소스)·`safeDbCall {}`·`safeDbFlow()`(DB)로 결과를 만들고(취소는 다시 던짐), domain UseCase 는 저장소가 준 결과를 그대로 돌려주고, presentation ViewModel 이 `when` 으로 세 상황을 판단한다(오류 문구는 원인과 관계없이 `:presentation:base` 의 `result_failure` 하나).
- **`:domain:<기능>` 의 `usecase` 는 사용자 행동 정의서다.** 사용자 행동 하나에 UseCase 하나를 두고 `operator fun invoke`로 부른다. `usecase/` 목록만 읽어도 이 앱으로 무엇을 할 수 있는지 알 수 있어야 한다. 저장소를 그대로 부르기만 하는 UseCase도 이 목적이면 만든다. ViewModel은 Repository가 아니라 UseCase만 부른다.
- **domain 은 결과를 `DomainResult`(Success·Fail·Error)로 돌려준다.** 상황만 전하고 원인을 나누지 않는다. 원인은 data 가 채우고(Fail 은 HTTP 코드, Error 는 원인 예외), 어떻게 보일지는 presentation 이 판단한다. 정렬처럼 보여 주는 방식도 presentation 이 정한다.
- `DomainResult` 를 받아 판단할 때는 `if (… is/!is …)`·`as?` 로 성공만 골라내지 않고 `when` 으로 Success·Fail·Error 를 모두 적는다. 처리가 같은 경우는 `is Fail, is Error ->` 처럼 한 줄에 묶는다. 각 상황을 어떻게 처리하는지가 코드에 드러나게 하려는 것이다.
- **domain 에는 비즈니스 로직을 두지 않는다.** UseCase 는 행동 이름과 입력만 정하고 저장소 함수 하나를 부른다. 데이터를 고르고 바꾸는 판단(필터·정렬·넣기/빼기)은 `:data:*`, 입력·표시 판단(검색어 공백 제거, 표시 가격)은 `:presentation:feature:<기능>` 이 맡는다.
- 패키지 이름은 모듈 경로 그대로다: `:data:api:searchbook` → `data.api.searchbook.*`, `:presentation:feature:search:main` → `presentation.feature.search.main`. `:app`만 `com.leeseungjun.booksearch`(applicationId)다.
- domain 은 출처(api·local)로 나누지 않고 주제로 나눈다. data 만 출처별(`api/<경로>`·`local/<저장 대상>`)이다.
- `:domain:<주제>` 은 `data`(DTO·enum) · `repo`(저장소 인터페이스) · `usecase` 세 패키지다. 화면 모듈은 쓰는 domain 모듈만 build.gradle.kts 에 적는다.
- `:data:*` 의 책임
  - `repo`: 원격과 로컬 중 어디서 가져올지 정하고(3초 시간 제한·캐시 대체), 데이터 소스가 준 원본(`~Api`·`~Entity`)을 DTO 로 바꾼다. Retrofit·Room 호출은 모른다.
  - `source/remote`: 서버 호출. 응답은 `~Api` 그대로 주고, 서버 호출은 `:data:base` 의 `safeApiCall { }` 로 감싼다 — HTTP 오류 → `DomainResult.Fail(code)`, 그 밖의 실패 → `DomainResult.Error(cause)`, 취소는 다시 던진다. 호출마다 try-catch 를 쓰지 않는다. `source/local`: DB·캐시 읽기/쓰기와 캐시 보관 규칙. `~Entity` 를 그대로 주고받는다.
  - `service`: Retrofit 인터페이스(`I~Service`). `data`: 서버 응답 데이터(`~Api`)와 그 변환 함수.
  - 데이터 소스는 자기 원본 타입(`~Api`·`~Entity`)만 주고받고, DTO 로 바꾸는 것은 `repo` 다. DTO 는 `repo` 부터 나타나고, `~Api`·`~Entity` 는 그 data 모듈 밖으로 나가지 않는다.
  - `~Api` 필드는 모두 nullable 이고 기본값을 두지 않는다. 서버가 안 보낸 값은 null 그대로 DTO 까지 가고, 어떻게 보일지는 화면이 정한다.
- Hilt 모듈은 바인딩을 구현한 모듈의 `di/` 패키지에 둔다. 별도 di 모듈은 두지 않는다.
- Hilt 범위: 저장소와 데이터 소스는 `ViewModelComponent` + `@ViewModelScoped`. `SingletonComponent` 는 앱에 하나여야만 동작하는 것(Room DB, OkHttp·Retrofit)에만 쓰고 이유를 주석으로 남긴다.

## 이름

| 대상 | 규칙 | 예 |
|---|---|---|
| 서버 응답 데이터(`:data/api`) | `~Api` | `SearchBookApi`, 안쪽 `DocumentApi` |
| Retrofit 인터페이스 | `I~Service` | `ISearchBookService` |
| Room 테이블 | `~Entity` | `BookEntity` |
| domain 데이터(data class) | `~DTO`. enum·예외는 붙이지 않는다 | `BookDTO`, `SearchSort` |
| 화면 상태 전체 | `~UiState` — ViewModel 의 MutableStateFlow 하나 | `SearchUiState` |
| 본문에 무엇을 보여 줄지(동시에 하나만: 첫 진입·로딩·결과·빈 결과·오류) | `~UiStatus` (sealed, UiState 의 `status`). 본문과 함께 성립하는 값(새로고침 중·다음 페이지·안내 줄)은 UiState 의 별도 칸 | `SearchUiStatus` |
| 화면 조각(`view.component`) | `<기능>~View` — 파일 이름도 같다. 기능 모듈 밖에서 쓰지 않으므로 `internal` | `DetailInfoView`, `SearchBookListView` |
| 화면 | `<기능>Screen`(ViewModel 연결, 라우터가 부르므로 public) / `<기능>Content`(상태만 받는 본문, `internal`) | `SearchScreen` / `SearchContent` |
| 목록 한 칸·블록 하나의 표시용 데이터 | `~ViewData` (mapper 가 만든다. 여러 화면이 쓰면 `:presentation:base`) | `BookViewData`, `DetailViewData` |
| 인터페이스 / 구현체 | `I~` / `~Impl` | `IBookRepository` / `BookRepositoryImpl` |
| 추상 클래스 / 베이스 클래스 | `Abs~` / `Base~` | `AbsBookDatabase` |

## 코드 순서
클래스 안의 선언 순서는 다음을 따른다.
1. 상수(`companion object`는 맨 아래, 상수만 둔다)
2. 프로퍼티: 주입받은 것 → private 상태 → 공개 상태(`StateFlow`)
3. `init`
4. 공개 함수(화면 이벤트 순서대로)
5. private 함수(호출하는 쪽 바로 아래에 둔다)

생성자로 주입받는 값은 한 번만 쓰더라도 모두 `private val` 로 받는다. 생성자 모양을 하나로 맞춘다.

## 상태
- ViewModel은 `MutableStateFlow<XxxUiState>` 하나를 갖고 `update {}`로만 바꾼다.
- 새로고침·다음 페이지·재시도는 함수(`refresh()`, `loadMore()`, `retry()`)로 부른다. Flow는 입력 디바운스와 Room 구독에만 쓴다.

## 결정·AI 활용 기록 — 항상 갱신한다
이 기록은 제출물이다. 아래 일이 생길 때마다 **그 턴 안에서** `docs/ai/decision-log.md`를 갱신한다.

| 일어난 일 | 기록할 절 |
|---|---|
| 작성자가 무언가를 결정하거나 AI 제안을 채택·변경·거절했다 | 1. 결정 기록 |
| 쟁점을 논의했지만 아직 결론이 나지 않았다 | 2. 논의 중 |
| 검토했던 안을 버렸다 | 3. 폐기한 안. 이유를 함께 적는다 |
| AI로 산출물을 만들었다 | 4. AI 활용 기록. 작성자가 어떻게 검토·검증했는지를 함께 적는다 |

- 사용자가 하지 않은 판단은 지어내지 않는다. 확인받지 않은 추천은 decision-log 1절 「세부 규칙」 표에 `추천안 기본 채택`으로 적고 작성자 검토를 받는다.
- AI 제안과 다르게 정한 항목은 `AI 제안과 다름`으로 표시한다. README의 「직접 판단해 바꾼 부분」은 이 표시가 있는 항목에서만 옮긴다.
- 일시는 `date`로 실측한다.
- 1절은 주제별 **현재 상태**로 쓴다. 같은 주제를 다시 정하면 새 행을 만들지 않고 그 행을 고친다. 사소한 결정은 묶어 짧게 쓴다. 사람이 읽고 판단할 수 있는 분량을 유지한다.
- 작성자의 개념·비교 질문(결정이 아닌 것)은 기록하지 않는다.
- 결정에 번호를 매기지 않는다. 코드 주석·커밋 메시지·다른 문서에서 결정 기록을 번호로 참조하지 않는다.

## 커밋·브랜치

| 브랜치 | 용도 |
|---|---|
| `master` | 제출 기준. 직접 커밋하지 않는다 |
| `release` | 제출 직전 안정화. `dev`에서 가져와 검증한 뒤 `master`로 병합한다 |
| `dev` | 통합 브랜치 |
| `feat/<기능>` | `dev`에서 따고, 끝나면 `dev`로 `--no-ff` 병합한 뒤 삭제한다 |

- 커밋 메시지는 머리말로 시작한다: `[feat]` 기능 · `[fix]` 버그 · `[refac]` 동작이 같은 구조 변경 · `[docs]` 문서 · `[merge]` 병합 · `[chore]` 빌드·설정.
- 코드 변경과 그 결정 기록(CLAUDE.md·decision-log 등)은 한 커밋으로 묶는다. 코드 커밋 뒤 문서 커밋을 따로 붙이지 않는다.
- 브랜치 이름은 영어 kebab-case. force push는 하지 않는다.
- `local.properties`, 빌드 산출물은 커밋하지 않는다.

## 테스트
- 테스트는 합의한 규칙이 조용히 깨지는 곳에만 둔다: data 의 결과 변환(safeApiCall·safeDbCall), 저장소의 오프라인 대체 순서, 응답 매퍼(매칭 키·null), 캐시 보관 규칙, DAO 쿼리. 화면(ViewModel·presentation mapper)은 구조가 자리 잡을 때까지 테스트를 두지 않고 실행으로 확인한다.
- 커밋 전에 `./gradlew testDebugUnitTest test` 가 통과해야 한다. 코드를 바꿔 테스트가 깨지면 같은 커밋에서 테스트를 고치거나, 더 지킬 규칙이 아니면 지운다.

## 주석
- 라이브러리를 고른 이유와 로직의 의도를 남긴다. "무엇"보다 "왜"를 쓴다. 코드를 읽으면 알 수 있는 내용은 쓰지 않는다.
- 주석에는 그 코드·파일에 대한 객관적인 사실만 쓴다. "어느 문서를 보라", "누가 이렇게 결정했다", 결정 기록 번호 같은 문서 참조는 쓰지 않는다.

## 빌드·명령

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew assembleDebug` |
| 단위 테스트 | `./gradlew testDebugUnitTest` |

요구 환경: JDK 17 · Android SDK Platform 37 · `local.properties`의 `KAKAO_REST_API_KEY`(키가 없어도 빌드는 통과)
