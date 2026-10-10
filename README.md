# BookSearch — 카카오 도서 검색

카카오 도서 검색 API 로 책을 찾고, 마음에 드는 책을 기기에 즐겨찾기로 저장하는 Android 앱입니다.
검색 탭 · 즐겨찾기 탭 · 상세 화면으로 이루어져 있고, 연결이 불안정할 때와 태블릿·폴더블 화면을 고려했습니다.

---

## 빌드 방법

| 항목 | 값 |
|---|---|
| JDK | 17 |
| Gradle / AGP | 9.4.1 (Wrapper 포함) / 9.2.1 |
| Kotlin / KSP | 2.4.0 / 2.3.10 |
| compileSdk · targetSdk / minSdk | 37 / 26 |

1. 루트의 `local.properties` 에 카카오 REST API 키를 넣습니다(제출물에는 포함되어 있습니다).
   ```properties
   KAKAO_REST_API_KEY=발급받은_REST_API_키
   ```
   키는 `:core:network` 가 빌드 때 `BuildConfig` 로 읽습니다. 키가 없어도 빌드는 되고, 검색이 401 로 실패해 오류 화면이 나옵니다.
2. 빌드와 테스트
   ```bash
   ./gradlew assembleDebug                 # app/build/outputs/apk/debug/app-debug.apk
   ./gradlew testDebugUnitTest test        # 단위 테스트(data 레이어 · DAO)
   ```

## 사용 프레임워크·라이브러리

| 분야 | 사용 |
|---|---|
| 언어·비동기 | Kotlin, Coroutines, Flow |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Material 3 Adaptive(NavigationSuiteScaffold · ListDetailSceneStrategy) |
| 화면 이동 | Navigation 3 (1.2.0) — 탭별 백스택을 직접 들고 한 `NavDisplay` 로 그림 |
| DI | Hilt 2.60.1 (KSP) |
| 네트워크 | Retrofit 3.0.0, OkHttp 5.5.0, kotlinx.serialization |
| 로컬 저장 | Room 2.8.4 |
| 이미지 | Coil 3 |
| 기타 | kotlinx.collections.immutable(화면 상태 안정성), Custom Tabs(전체 책 소개) |
| 테스트 | JUnit4, kotlinx-coroutines-test, Robolectric(DAO 쿼리를 JVM 에서 실행) |

## 프로젝트 구조

레이어(presentation · domain · data) × 기능으로 나눈 17개 모듈입니다. 공통 빌드 설정은 `build-logic` 의 컨벤션 플러그인에 있습니다.

```
:app                                   단일 Activity, 탭 골격, 화면 등록을 모아 NavDisplay 로 그림, 600dp 이상 목록-상세 2칸
:core:designsystem                     테마 · 간격 토큰 · 두 화면 이상이 쓰는 UI
:core:navigation                       INavigator · EntryProviderInstaller (기능과 :app 의 화면 이동 계약)
:core:network                          OkHttp · Retrofit · API 키 (키를 아는 유일한 모듈)
:presentation:base                     책 카드 · 카드 표시 값 변환 · 공통 문구
:presentation:router                   화면별 XxxRouter(PageData) — 다른 기능 화면으로 갈 때 이것만 씀
:presentation:feature:{search,favorite,detail}:main
                                       vm/ · view/ · view/component/ · data/(UiState) · mapper/ · router/ · di/
:domain:base                           BookDTO · DomainResult
:domain:{search,favorite,book}         data/ · repo/(저장소 인터페이스) · usecase/ — 순수 Kotlin
:data:base                             결과 변환(safeApiCall · safeDbCall) · Room DB(db/) · Entity ↔ DTO
:data:api:searchbook                   검색 API: service/ · data/(~Api) · source/(remote · local 캐시) · repo/ · di/
:data:local:{favorite,book}            기기 DB 만 쓰는 기능: source/ · repo/ · di/
```

- 의존 방향은 `presentation → domain ← data` 입니다. presentation 은 data 를 모르고, 기능 data 모듈과 `:core:network` 는 Hilt 그래프를 만드는 `:app` 만 압니다.
- 기능 main 모듈끼리는 서로 의존하지 않습니다. 화면 이동은 `:presentation:router` 의 Router 를 주입받아 `open(PageData)` 만 부르고, 화면 등록은 각 모듈이 Hilt(`@IntoSet`)로 내놓아 `:app` 이 모읍니다. 새 화면을 더해도 다른 기능 모듈은 고치지 않습니다.
- `:domain:<기능>/usecase` 는 **사용자 행동 정의서**입니다. 행동 하나에 UseCase 하나라, 목록만 읽어도 앱으로 무엇을 할 수 있는지 보입니다.
- 데이터 소스는 자기 원본(`~Api` · `~Entity`)만 주고받고, DTO 로 바꾸는 것과 "원격·로컬 중 어디서 가져올지"는 저장소가 정합니다.
- 결과는 `DomainResult`(Success · Fail(HTTP 코드) · Error(원인))로 전하고, 받는 쪽은 `when` 으로 세 상황을 모두 적습니다.

구조는 [`ARCHITECTURE.md`](ARCHITECTURE.md) 와 레이어별 README, 코드 규칙(이름 · 패키지 · 코드 순서 · 주석 · 테스트 범위)은 [`CLAUDE.md`](CLAUDE.md) 에 있습니다.

## 주요 구현 포인트

**검색 탭**
- 입력이 400ms 멈추면 검색합니다. 고쳤다가 되돌린 검색어는 다시 받지 않고, 새 검색이 이전 요청을 취소해 늦게 온 응답이 새 결과를 덮지 않습니다.
- 정확도순 · 발간일순 정렬, 20개씩 페이징. 목록 끝에서 10칸 남았을 때 다음 페이지를 불러 빠르게 내려도 끊기지 않게 했습니다.
- 마지막 페이지를 넘기면 서버가 같은 페이지를 다시 보내므로, 이미 받은 책을 빼고 새 책이 없으면 끝으로 봅니다.
- 스크롤하면 검색창이 접히고 총 개수 · 정렬 줄만 남습니다. 당겨서 새로고침, 맨 위로 버튼이 있습니다.

**즐겨찾기 탭**
- 기기(Room)에만 저장합니다. 책 정보는 `book` 테이블에 한 번만 두고 즐겨찾기는 키와 저장 시각만 둡니다.
- 제목 · 저자 로컬 검색, 금액 구간 필터(1만 원 단위), 제목 오름 · 내림차순을 DB 쿼리 하나로 처리합니다.

**상세**
- 책을 식별 키로 기기 DB 에서 다시 읽습니다(검색 결과의 책은 저장되어 있음). 화면 회전 · 프로세스 재시작 뒤에도 같은 책을 보여 줍니다(6페이지 이후의 책은 다음 새 검색 때 정리될 수 있음).
- 책 소개는 API 가 약 250자 요약만 주므로, 잘렸을 때만 "…" 를 붙이고 '전체 소개 보기'로 원문 페이지를 Custom Tab 으로 엽니다.

**불안정한 네트워크**

| 상황 | 동작 |
|---|---|
| 요청이 3초를 넘기거나 연결이 없고, 같은 검색을 저장해 둠 | 저장된 결과 + "저장된 결과 · 마지막 갱신 시각" 안내 줄 |
| 같은 검색의 저장 결과가 없음(첫 페이지) | 기기에 저장된 모든 책에서 검색어로 찾은 결과 + 안내 줄 |
| 둘 다 없음 / 서버 오류 / 키 오류 | 공통 문구 "네트워크 또는 서버가 원활하지 않아요. 잠시 후 다시 시도해 주세요" + 다시 시도 |
| 다음 페이지 실패 | 받은 목록은 그대로, 목록 끝에서만 다시 시도 |
| 새로고침 실패 | 보던 목록 유지 |

- 연결 상태를 미리 살피지 않고 요청 실패로만 판단합니다. 연결은 있지만 느린 경우(지하철)는 연결 상태로 알 수 없어 시간 제한이 어차피 필요하기 때문입니다.
- 검색 결과는 (검색어 · 정렬) 최근 20조합 × 5페이지를 저장합니다. 자동 재시도는 하지 않습니다(할당량 · 사용자가 누르는 다시 시도로 충분).
- 오류 문구에는 서버 응답 원문을 쓰지 않습니다(401 응답 본문에 키 일부가 들어 있음). 통신 로그는 debug 빌드에만 남기고 인증 헤더는 가립니다.

**태블릿 · 폴더블**
- 창 너비 600dp 이상이면 목록과 상세를 나란히 두고(카드를 누르면 오른쪽 칸만 바뀜), 하단 탭바 대신 측면 레일을 씁니다.
- 접기 · 펼치기 · 회전 뒤에도 검색어 · 스크롤 · 선택한 책 · 탭별 화면이 유지됩니다(탭별 백스택을 모두 한 `NavDisplay` 에 두어 상태를 남김).
- 본문은 시스템 내비 바 · 키보드 위에서 끝납니다. 키보드가 떠 있는 동안 하단 탭바를 숨깁니다.
- 뒤로 가기는 지금 탭 안에서만 움직입니다(2칸이면 상세만 닫힘, 즐겨찾기 첫 화면 → 검색 탭, 검색 첫 화면 → 홈).

## 고려했지만 구현하지 않은 것

| 항목 | 왜 고려했나 | 왜 하지 않았나 |
|---|---|---|
| 느린 연결에서 원래 요청 계속 기다리기 | 응답이 늘 3초를 넘는 연결에서는 네트워크 결과를 받지 못한다 | 저장 결과를 보던 중 응답이 와서 목록 전체가 바뀌면 보던 위치 · 항목이 흔들린다. 3초 제한을 한계로 둔다 |
| 연결 상태 사전 감지 | 오프라인이면 바로 캐시 | data 레이어가 Context 를 갖게 된다. 3초 시간 제한이 같은 효과를 낸다 |
| 자동 재시도 · 백오프 | 5xx 일시 오류 | 다시 시도 버튼으로 충분하고 API 할당량을 쓴다 |
| 검색 결과 캐시 만료 | 오래된 데이터 | 오프라인에서 보여 줄 수 있는 가치가 더 크다. 안내 줄에 저장 시각을 보인다 |
| 즐겨찾기 서버 동기화 | 기기 간 공유 | API 에 없고 요구사항은 로컬 저장 |
| 책 소개 전문 | 요약이 약 250자에서 잘린다 | 웹 페이지 긁어오기는 약관 · 구조 변경 위험. Custom Tab 으로 원문을 연다 |
| 최근 검색어 | 입력 부담 줄이기 | 요구사항 범위 밖 |
| 다크 모드 · 큰 글씨 점검 | 접근성 | 시스템 설정을 따르는 데까지. 별도 검증은 하지 않았다 |
| 창 크기 연속 조절(멀티 윈도우) 세분화 | 폴더블 · 태블릿 | 600dp 한 경계만 둔다. 더 나누려면 사용 데이터가 필요하다 |
| 6페이지 이후 책의 상세 보존 | 검색 캐시는 5페이지까지만 남겨, 6페이지 이후 책은 다음 새 검색 때 정리된다. 그 책의 상세를 연 채 프로세스가 재시작되면 정보가 없다고 나온다 | 드문 경우다. 막으려면 열어 본 책을 따로 표시해 정리에서 빼야 해 코드가 는다 |
| 화면(ViewModel) 테스트 | 회귀 방지 | 구조가 자리 잡을 때까지 실행으로 확인하고, 테스트는 조용히 깨지기 쉬운 data 레이어(결과 변환 · 오프라인 대체 순서 · 응답 매퍼 · 캐시 규칙 · DAO 쿼리)에만 둔다 |

## 이어받는 사람을 위한 문서와 에이전트

**문서** — 무엇이 어떻게 되어 있는지와 왜 그렇게 했는지를 나눠 둡니다.

| 문서 | 내용 |
|---|---|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | 앱 전체 구조: 설계 원칙, 모듈 지도, 데이터 흐름, 화면 이동, DI 범위 |
| [`app/`](app/README.md) · [`core/`](core/README.md) · [`presentation/`](presentation/README.md) · [`domain/`](domain/README.md) · [`data/`](data/README.md) 의 README | 레이어별 모듈 · 패키지 · 동작, 새로 만들 때 순서 |
| [`docs/guide/기능-추가.md`](docs/guide/기능-추가.md) | 새 기능을 더하는 순서와 확인 목록 |
| [`CLAUDE.md`](CLAUDE.md) | 코드 규칙(이름 · 순서 · 주석 · 테스트 · 커밋). 사람과 AI 에이전트가 함께 따른다 |
| [`docs/ai/decision-log.md`](docs/ai/decision-log.md) | 주제별 결정과 그 이유, AI 활용 기록 |
| [`docs/plan/기획서.md`](docs/plan/기획서.md) | 사용자 행동 ↔ UseCase, 네트워크 시나리오, 요구사항 추적표, 고려했지만 구현하지 않은 것 |
| [`docs/api.md`](docs/api.md) | 카카오 API 를 실제로 호출해 확인한 사실 |

**에이전트** — 저장소의 `.claude/agents/` 에 있어, Claude Code 로 이 저장소를 열면 바로 쓸 수 있습니다.

| 에이전트 | 하는 일 |
|---|---|
| `onboarding` | 구조 문서와 코드만 근거로 질문에 답합니다. 근거 위치를 함께 알려 주고, 모르면 모른다고 하며, 파일은 고치지 않습니다 |
| `feature-dev` | `docs/guide/기능-추가.md` 와 `CLAUDE.md` 를 따라 새 기능을 만듭니다. 만들 파일 목록을 먼저 확인받고 단계별로 구현 · 검증합니다 |

## AI 활용

Claude Code 를 썼습니다. 메인 세션 외에 직접 만든 에이전트 두 개(기획서 작성 `plan-haul`, 단계 구현 `dev-haul`)와 API 실측 스킬, 리뷰용 에이전트를 함께 썼습니다. 결정과 경위는 [`docs/ai/decision-log.md`](docs/ai/decision-log.md) 에 주제별로 남겼고, 아래는 그 요약입니다.

### 어떤 작업에 AI 를 활용했는지

| 작업 | AI 가 한 일 |
|---|---|
| 첫 버전 폐기와 재설계 | 폐기 원인 정리, 구조안 비교, 단계별 진행안 |
| 기획서 | 요구사항 정규화, 사용자 행동 13개 ↔ UseCase, 화면 정의, 네트워크 시나리오, 요구사항 추적표 |
| API 확인 | 실제 호출로 응답 형태 · 누락 필드 · 경계 동작(마지막 페이지 반복, 할인가 -1, 오류 본문) 확인 |
| 구현 | 모듈 · 컨벤션 플러그인 골격, data · domain 계층, 검색 · 즐겨찾기 · 상세 화면, 2칸 배치 · 전환, 단위 테스트 |
| 리뷰 | 요구사항 대조표, 버그 후보, 규칙 위반, 문서와 코드 불일치 |

### AI 결과를 어떻게 검증했는지

- **단계마다 승인 → 검토 → 병합**: 단계 시작 전 만들 파일 목록을 승인하고, 끝나면 diff 를 읽은 뒤 기능 브랜치를 `dev` 에 병합했습니다.
- **빌드 · 테스트**: 모든 변경에서 `./gradlew testDebugUnitTest test` 와 `assembleDebug` 를 통과시켰습니다.
- **실행 확인**: 에뮬레이터(폴더블 접기 · 펼치기, 3버튼 내비, 오프라인 전환)와 실기기에서 화면을 직접 확인했습니다. 실기기에서 작업 표시줄이 목록 끝을 가리는 문제와 뒤로 스와이프 미리보기 문제를 찾아 고쳤습니다.
- **문서보다 실측**: API 는 문서 대신 실제 호출 결과를 기준으로 삼았습니다(`docs/api.md`).
- **AI 단독 결정 검토**: AI 가 확인 없이 정한 세부 규칙은 따로 모아 작성자가 목록으로 검토한 뒤 확정했습니다(`decision-log.md` 「세부 규칙」).

### AI 결과를 그대로 쓰지 않고 직접 판단해 바꾼 부분

| 주제 | AI 제안 | 작성자 결정과 이유 |
|---|---|---|
| 첫 버전 | AI(dev-haul)가 만든 모듈 15개 · Kotlin 119개 파일 | 구조가 과하고 한 줄씩 설명할 수 없어 폐기하고 다시 만듦 |
| 진행 방식 | 메인 세션이 직접 구현 | 만든 에이전트의 활용도 보이도록 기획 · 구현을 에이전트에 단계 위임하고, 에이전트 자체도 실패 사례에 맞게 고침 |
| 모듈 | 기능 모듈 우선(`:feature:*`) | 레이어 × 기능. domain 은 데이터 출처와 무관하게 주제별, data 는 출처(API · 로컬)별. DB 는 앱 데이터를 담는 data 의 일이라 `:data:base` 에 둠 |
| 화면 이동 | 화면은 콜백만 내놓고 `:app` 이 모든 이동을 연결 | `:app` 이 기능 전체를 알면 의존 그래프가 무너진다 → 공용 router 모듈 + 화면별 Router |
| data 내부 | 저장소가 API · DAO 를 직접 호출, 데이터 소스가 DTO 로 변환 | 데이터 소스를 분리하고, 데이터 소스는 원본만 넘기고 변환은 저장소가. 반복되던 try-catch 는 `safeApiCall` 하나로 |
| domain 의 역할 | 단순 위임 UseCase 는 만들지 말고 필터는 UseCase 안에, 결과는 `Result` + 원인 enum | UseCase 는 사용자 행동 정의서, 판단 로직은 data · presentation 으로. 결과는 상황만 전하는 `DomainResult`, 받는 쪽은 `when` 으로 세 상황을 모두 적음 |
| UseCase 구성 | 즐겨찾기 토글을 추가 · 삭제 둘로 | 행동이 하나라 `ToggleFavoriteUseCase` 하나. 다음 페이지도 같은 저장소 함수라 `SearchBooksUseCase` 하나 |
| DI 범위 | 저장소 `@Singleton` | 저장소 · 데이터 소스는 ViewModel 범위. 싱글톤은 하나여야 동작하는 Room · OkHttp · Retrofit 만 |
| 이름 | `~Response`, 접미사 없는 모델, 접두사 없는 인터페이스 | `~Api` · `~Entity` · `~DTO` · `~UiState` · `I~` · `~Impl` 규칙, 화면 조각은 `<기능>~View` |
| 응답 null 처리 | 기본값(`""` · 0 · -1)을 넣고 빈 값을 null 로 | 빈 값으로 판단하면 예외가 많다 → 서버 값을 그대로 믿고 null 은 화면이 판단 |
| 실패 시 동작 | 통신 계층에서 GET 2회 자동 재시도, 키 오류는 따로 알림 | 다시 시도 버튼, 오류 문구는 하나. 오프라인 새 검색어는 저장된 모든 책에서 찾기를 더함. "캐시 먼저 보이고 응답 기다리기"는 목록이 통째로 바뀌는 게 더 나빠 하지 않음 |
| 다음 페이지 요청 시점 | 끝에서 5칸 | 실행해 보니 빠르게 내리면 끊김 → 10칸 |
| 머리 접힘 | 목록이 맨 위에 닿아야 펼침 | 실행해 보니 올려도 안 내려옴 → 손가락 방향을 머리가 먼저 받게 |
