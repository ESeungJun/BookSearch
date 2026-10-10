# :presentation — 화면

사용자가 보는 화면과 그 상태를 둡니다. domain 의 UseCase 만 부르고, data 레이어는 모릅니다.

| 모듈 | 하는 일 |
|---|---|
| `:presentation:base` | 여러 화면이 함께 쓰는 것 중 domain 타입(`BookDTO`)을 아는 것: 책 카드, 하트 버튼, 카드 표시 값 변환, 공통 문구, 토스트 |
| `:presentation:router` | 화면별 `XxxRouter`. 다른 기능 화면으로 갈 때 이것만 쓴다 |
| `:presentation:feature:search:main` | 검색 탭 |
| `:presentation:feature:favorite:main` | 즐겨찾기 탭 |
| `:presentation:feature:detail:main` | 책 상세 |

## 기능 모듈의 패키지

```
presentation.feature.<기능>.main
├─ vm/              XxxViewModel — 화면 상태 하나를 만든다
├─ view/            XxxScreen(ViewModel 연결) · XxxContent(상태만 받아 그림)
│  └─ component/    화면 조각. 파일 하나에 조각 하나와 그 @Preview
├─ data/            XxxUiState · XxxUiStatus · 화면 전용 타입
├─ mapper/          DTO → 화면 값 변환(확장 함수)
├─ router/          XxxRouterImpl (다른 화면이 이 화면으로 올 때만)
└─ di/              Router 바인딩, "PageData → 화면" 등록
```

## 화면 상태

- ViewModel 은 `MutableStateFlow<XxxUiState>` 하나를 갖고 `update {}` 로만 바꿉니다. 화면은 그 상태 하나를 그립니다.
- `XxxUiStatus` 는 본문에 무엇을 보일지(첫 진입 · 로딩 · 결과 · 빈 결과 · 오류) 중 하나입니다. 본문과 함께 성립하는 값(새로고침 중, 다음 페이지 상태, 안내 줄, 토스트)은 `UiState` 의 별도 칸입니다.
- 새로고침 · 다음 페이지 · 다시 시도는 함수(`refresh()` · `loadMore()` · `retry()`)로 부릅니다. Flow 는 입력 디바운스와 DB 구독에만 씁니다.
- UseCase 결과(`DomainResult`)는 `when` 으로 성공 · 실패 · 에러를 모두 적어 판단합니다. 실패 문구는 원인과 관계없이 하나(`result_failure`)입니다.
- 검색어 · 정렬 · 필터처럼 사용자가 고른 조건은 `SavedStateHandle` 에도 둡니다. 백그라운드에서 프로세스가 정리됐다 돌아와도 같은 조건으로 다시 받습니다.
- 2칸에서는 화면이 `LocalCurrentRoute`(`:core:navigation`)에서 상세에 열린 책 키를 읽어 그 카드를 표시합니다.
- 토스트처럼 한 번만 보일 것은 `UiState.toastRes` 에 두고, 화면이 보인 뒤 `onToastShown()` 으로 지웁니다(`ToastEffect`).

## 화면별 요점

**검색** (`SearchViewModel`)
- 입력이 400ms 멈추면 검색합니다. 마지막으로 검색한 검색어와 같으면(고쳤다가 되돌림) 다시 받지 않습니다.
- 첫 페이지 · 다음 페이지 요청은 한 번에 하나입니다. 새 요청이 이전 요청을 취소해 늦게 온 응답이 새 결과를 덮지 않습니다.
- 목록 끝에서 10칸 남으면 다음 페이지를 부릅니다. 이미 받은 책은 빼고, 새 책이 없으면 끝으로 봅니다.
- 사용자가 보는 것:

  | 상황 | 화면 |
  |---|---|
  | 네트워크 실패, 같은 검색의 저장 결과 있음 | 목록 + 안내 줄 "저장된 결과 · 마지막 갱신 …"(`SearchNotice.Cached`) |
  | 네트워크 실패(연결 없음·시간 초과·서버 오류), 저장된 책에서 찾음 | 목록 + 안내 줄 "연결이 원활하지 않아 저장된 책에서 찾았어요"(`SearchNotice.LocalMatch`) |
  | 둘 다 없음 · 서버 오류 | 오류 화면(공통 문구) + 다시 시도 |
  | 다음 페이지 실패 | 목록 끝에 "목록을 더 불러오지 못했어요" + 다시 시도 |
  | 새로고침 실패 | 보던 목록 유지 |

**즐겨찾기** (`FavoriteViewModel`)
- 검색어 · 금액 구간이 바뀌면 DB 구독을 새 조건으로 바꿉니다(`flatMapLatest`). 정렬(제목 오름 · 내림차순)은 ViewModel 이 합니다(DB 는 최근 저장순으로 준다).
- 조건에 맞는 책과 전체 수를 함께 받아 "m / n권"과 빈 화면 종류(저장 0권 · 조건 불일치)를 정합니다.

**상세** (`DetailViewModel`)
- 책을 식별 키(`bookId`)로 기기 DB 에서 읽습니다. 키는 `@AssistedInject` 로 ViewModel 을 만들 때 넘깁니다. 같은 상세 화면이 책마다 따로 열리고 회전 뒤에도 같은 책을 다시 읽기 위해서입니다.
- '전체 소개 보기'는 원문 페이지를 Custom Tab 으로 엽니다.

## 이름

| 대상 | 규칙 | 예 |
|---|---|---|
| 화면 | `<기능>Screen`(public) · `<기능>Content`(internal) | `SearchScreen` |
| 화면 조각 | `<기능>~View`(internal), 파일 이름도 같다 | `DetailInfoView` |
| 화면 상태 | `~UiState` · `~UiStatus` | `SearchUiState` |
| 화면 값 | `~ViewData` | `BookViewData` |

## 새 화면을 만들 때

1. `presentation/feature/<기능>/main` 모듈을 만들고 `convention.presentation` 플러그인을 붙입니다. 쓰는 domain 모듈만 의존에 적습니다.
2. 다른 화면이 이 화면으로 와야 하면 `:presentation:router` 에 `XxxRouter`(PageData + open)를 더하고, 이 모듈의 `router/` 에 구현을 둡니다.
3. `di/` 에서 Router 를 바인딩하고 "PageData → 화면"을 `@IntoSet` 으로 내놓습니다.
4. `:app` 의 build.gradle.kts 에 모듈을 더합니다.

자세한 순서는 [`docs/guide/기능-추가.md`](../docs/guide/기능-추가.md) 에 있습니다.
