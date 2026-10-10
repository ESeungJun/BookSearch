# :domain — 사용자 행동 정의

앱으로 할 수 있는 일을 정의합니다. 순수 Kotlin 이고 Android · Retrofit · Room 을 모릅니다.

| 모듈 | 내용 |
|---|---|
| `:domain:base` | 공통 타입: `BookDTO`(책 한 권), `DomainResult`(결과) |
| `:domain:search` | 책 검색 |
| `:domain:favorite` | 즐겨찾기 |
| `:domain:book` | 책 한 권 조회(상세) |

모듈은 데이터 출처(API · 기기)가 아니라 주제로 나눕니다. domain 은 데이터가 어디서 오는지 관심이 없습니다. 주제 모듈끼리는 서로 의존하지 않습니다.

## 패키지

```
domain.<주제>
├─ data/      ~DTO · enum (화면과 data 가 주고받는 값)
├─ repo/      I~Repository (저장소 인터페이스. 구현은 data 레이어)
└─ usecase/   ~UseCase (사용자 행동 하나에 하나)
```

## UseCase = 사용자 행동 정의서

`usecase/` 목록만 읽어도 이 앱으로 무엇을 할 수 있는지 알 수 있게 합니다.

| UseCase | 사용자 행동 |
|---|---|
| `SearchBooksUseCase(query, sort, page)` | 검색어 · 정렬로 책을 찾는다(새 검색 · 정렬 변경 · 새로고침 · 다음 페이지) |
| `ObserveFavoritesUseCase(query, priceRange)` | 조건에 맞는 즐겨찾기 목록을 본다 |
| `ObserveFavoriteKeysUseCase()` | 어떤 책이 즐겨찾기인지 본다(하트 표시) |
| `ToggleFavoriteUseCase(book, isFavorite)` | 즐겨찾기에 넣거나 뺀다 |
| `GetBookUseCase(key)` | 책 한 권의 상세를 본다 |

- 행동 하나에 `operator fun invoke` 하나를 두고, 저장소 함수 하나를 부릅니다. 저장소를 그대로 부르기만 해도 행동을 드러내기 위해 만듭니다.
- 판단 로직은 두지 않습니다. 데이터를 고르고 바꾸는 판단(필터 · 캐시 대체)은 data, 입력 · 표시 판단(공백 제거 · 정렬 · 표시 가격)은 presentation 이 맡습니다.
- ViewModel 은 저장소가 아니라 UseCase 만 부릅니다.

## 결과 타입

저장소 인터페이스의 모든 함수는 `DomainResult` 를 돌려줍니다(관찰은 `Flow<DomainResult<T>>`).

| 경우 | 뜻 |
|---|---|
| `Success(data)` | 성공 |
| `Fail(code)` | 서버가 응답했지만 성공이 아님. HTTP 코드만 담는다(401 응답 본문에 키 일부가 있어 본문은 넘기지 않음) |
| `Error(cause)` | 연결 실패 · 시간 초과 · 형식 오류 · DB 오류 |

domain 은 상황만 전하고 원인을 나누지 않습니다. 어떻게 보일지는 화면이 정합니다.

## 빌드

`convention.domain` 플러그인이 순수 Kotlin 설정과 `:domain:base` 를 붙입니다. UseCase 의 Dagger 팩토리를 이 모듈에서 한 번만 만들도록 dagger-compiler 도 붙입니다. 없으면 같은 UseCase 를 쓰는 화면 모듈마다 팩토리를 따로 만들어 앱 빌드가 중복 클래스로 실패합니다.

## 새 행동을 더할 때

1. 주제에 맞는 모듈의 `repo/` 인터페이스에 함수를 더합니다(돌려주는 값은 `DomainResult`).
2. `usecase/` 에 행동 이름으로 UseCase 를 만듭니다.
3. 새 주제면 `domain/<주제>` 모듈을 만들고 `convention.domain` 을 붙입니다.
