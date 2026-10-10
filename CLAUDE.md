# BookSearch — 작업 규칙

카카오 도서 검색 API로 만드는 Android 채용 과제다. Kotlin, Coroutine/Flow, Jetpack Compose를 쓴다.
요구사항 원문은 `docs/requirements/assignment.md`, API 실측은 `docs/api.md`에 있다.
이 저장소는 두 번째 버전이다. 첫 버전을 폐기한 이유는 `docs/ai/v1-retrospective.md`에 있다.

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

```
:app                              MainActivity · navigation/ · 탭·2칸 Scaffold
:core:designsystem                테마·두 화면 이상이 쓰는 UI. 공통 코드는 패키지 단위 모듈(:core:<이름>)로, 쓸 것이 생길 때 만든다
:di:network                       OkHttp·Retrofit·API 서비스 제공(싱글톤), API 키 BuildConfig — 키를 아는 유일한 모듈
:di:database                      Room DB·DAO 제공(싱글톤)
:di:search / favorite / detail    Hilt 모듈 — 기능별 바인딩
:presentation:search / favorite / detail    Screen · ViewModel · UiState
:domain:base                      공통 타입만 — BookDTO · DomainResult
:domain:search / favorite / detail   기능별 data/ · repo/ · usecase/ (순수 Kotlin). 서로 의존하지 않는다
:data:base                        원격 데이터 소스 공통 코드 — apiCall {} (서버 호출 → DomainResult)
:data:database                    Room DB · dao/ · entity/ 만 — Room 이 한곳에서 모든 테이블을 알아야 해서 공통 인프라로 둔다
:data:search / favorite / detail  repo/ · source/(remote·local) · service/ · data/ — 기능별, :data:database 의 DAO 를 쓴다
```

- 의존 방향: `presentation → domain ← data`. `:di:*`만 `:data:*`를 의존한다. presentation 은 data 를 볼 수 없다.
- 기능 모듈끼리 서로 의존하지 않는다. 화면 이동은 `:app`이 연결한다.
- **`:domain/usecase`는 사용자 행동 정의서다.** 사용자 행동 하나에 UseCase 하나를 두고 `operator fun invoke`로 부른다. `usecase/` 목록만 읽어도 이 앱으로 무엇을 할 수 있는지 알 수 있어야 한다. 저장소를 그대로 부르기만 하는 UseCase도 이 목적이면 만든다. ViewModel은 Repository가 아니라 UseCase만 부른다.
- **domain 은 결과를 `DomainResult`(Success·Fail·Error)로 돌려준다.** 상황만 전하고 원인을 나누지 않는다. 원인은 data 가 채우고(Fail 은 HTTP 코드, Error 는 원인 예외), 어떻게 보일지는 presentation 이 판단한다. 정렬처럼 보여 주는 방식도 presentation 이 정한다.
- **domain 에는 비즈니스 로직을 두지 않는다.** UseCase 는 행동 이름과 입력만 정하고 저장소 함수 하나를 부른다. 데이터를 고르고 바꾸는 판단(필터·정렬·넣기/빼기)은 `:data:<기능>`, 입력·표시 판단(검색어 공백 제거, 표시 가격)은 `:presentation:<기능>` 이 맡는다.
- 패키지 이름은 모듈 경로 그대로다: `:data` → `data.*`, `:presentation:search` → `presentation.search`. `:app`만 `com.leeseungjun.booksearch`(applicationId)다.
- `:domain:<기능>` 은 `data`(DTO·enum) · `repo`(저장소 인터페이스) · `usecase` 세 패키지다. 화면 모듈은 쓰는 domain 모듈만 build.gradle.kts 에 적는다.
- `:data:*` 의 책임
  - `repo`: 원격과 로컬 중 어디서 가져올지만 정한다(3초 시간 제한·캐시 대체). Retrofit·Room 을 모른다.
  - `source/remote`: 서버 호출. `~Api` → DTO 변환, 서버 호출은 `:data:base` 의 `apiCall { }` 로 감싼다 — HTTP 오류 → `DomainResult.Fail(code)`, 그 밖의 실패 → `DomainResult.Error(cause)`, 취소는 다시 던진다. 호출마다 try-catch 를 쓰지 않는다. `source/local`: DB·캐시 읽기/쓰기와 캐시 보관 규칙. `~Entity` ↔ DTO 변환.
  - `service`: Retrofit 인터페이스(`I~Service`). `data`: 서버 응답 데이터(`~Api`)와 그 변환 함수. `db/dao`·`db/entity`: Room.
  - `~Api` 는 `source/remote` 밖으로, `~Entity` 는 `source/local` 밖으로 나가지 않는다. 데이터 소스는 DTO 로 주고받는다.
  - `~Api` 필드는 모두 nullable 이고 기본값을 두지 않는다. 서버가 안 보낸 값은 null 그대로 DTO 까지 가고, 어떻게 보일지는 화면이 정한다.
- Hilt 범위: 저장소와 데이터 소스는 `ViewModelComponent` + `@ViewModelScoped`. `SingletonComponent` 는 앱에 하나여야만 동작하는 것(Room DB, OkHttp·Retrofit)에만 쓰고 이유를 주석으로 남긴다.

## 이름

| 대상 | 규칙 | 예 |
|---|---|---|
| 서버 응답 데이터(`:data/api`) | `~Api` | `SearchBookApi`, 안쪽 `DocumentApi` |
| Retrofit 인터페이스 | `I~Service` | `ISearchBookService` |
| Room 테이블 | `~Entity` | `BookEntity` |
| domain 데이터(data class) | `~DTO`. enum·예외는 붙이지 않는다 | `BookDTO`, `SearchSort` |
| 화면 상태 / 목록 한 칸의 표시용 데이터 | `~UiState` / `~ViewData` | `SearchUiState`, `BookViewData` |
| 인터페이스 / 구현체 | `I~` / `~Impl` | `IBookRepository` / `BookRepositoryImpl` |
| 추상 클래스 / 베이스 클래스 | `Abs~` / `Base~` | `AbsBookDatabase` |

## 코드 순서
클래스 안의 선언 순서는 다음을 따른다.
1. 상수(`companion object`는 맨 아래, 상수만 둔다)
2. 프로퍼티: 주입받은 것 → private 상태 → 공개 상태(`StateFlow`)
3. `init`
4. 공개 함수(화면 이벤트 순서대로)
5. private 함수(호출하는 쪽 바로 아래에 둔다)

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

- 사용자가 하지 않은 판단은 지어내지 않는다. 확인받지 않은 추천은 `추천안 기본 채택`으로 표시하고 `docs/ai/REVIEW.md`에 모은다.
- AI 제안과 다르게 정한 항목은 `AI 제안과 다름`으로 표시한다. README의 「직접 판단해 바꾼 부분」은 이 표시가 있는 항목에서만 옮긴다.
- 일시는 `date`로 실측한다.
- 1절은 주제별 **현재 상태**로 쓴다. 같은 주제를 다시 정하면 새 행을 만들지 않고 그 행을 고치고 ID 를 덧붙인다. 사소한 결정은 묶어 짧게 쓴다. 사람이 읽고 판단할 수 있는 분량을 유지한다.
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

## 주석
- 라이브러리를 고른 이유와 로직의 의도를 남긴다. "무엇"보다 "왜"를 쓴다. 코드를 읽으면 알 수 있는 내용은 쓰지 않는다.
- 주석에는 그 코드·파일에 대한 객관적인 사실만 쓴다. "어느 문서를 보라", "누가 이렇게 결정했다", 결정 기록 번호 같은 문서 참조는 쓰지 않는다.

## 빌드·명령

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew assembleDebug` |
| 단위 테스트 | `./gradlew testDebugUnitTest` |

요구 환경: JDK 17 · Android SDK Platform 37 · `local.properties`의 `KAKAO_REST_API_KEY`(키가 없어도 빌드는 통과)
