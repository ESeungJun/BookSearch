# :data — 데이터 가져오기와 저장

domain 의 저장소 인터페이스를 구현합니다. 서버(Retrofit)와 기기 DB(Room)를 아는 유일한 레이어입니다.

| 모듈 | 내용 |
|---|---|
| `:data:base` | 결과 변환(`safeApiCall` · `safeDbCall` · `safeDbFlow`), Room DB(`db/`), Entity ↔ DTO |
| `:data:api:searchbook` | 카카오 도서 검색 API. 검색 결과 캐시와 오프라인 대체 |
| `:data:local:favorite` | 즐겨찾기(기기 DB 만) |
| `:data:local:book` | 책 한 권 조회(기기 DB 만) |

data 모듈은 출처로 나눕니다: 서버 API 하나는 `api/<API 경로>`, 기기 DB 만 쓰는 기능은 `local/<저장 대상>`. 기능 data 모듈끼리는 서로 의존하지 않고 `:data:base` 만 봅니다.

## 패키지

```
data.<출처>.<이름>
├─ repo/             ~RepositoryImpl — 어디서 가져올지 정하고, 원본을 DTO 로 바꾼다
├─ source/remote/    I~RemoteDataSource · ~Impl — 서버 호출, 응답(~Api)을 그대로 준다
├─ source/local/     I~LocalDataSource · ~Impl — DB 읽기 · 쓰기, 행(~Entity)을 그대로 주고받는다
├─ service/          I~Service — Retrofit 인터페이스 (api 모듈만)
├─ data/             ~Api — 서버 응답 모양과 그 변환 함수 (api 모듈만)
└─ di/               Hilt 바인딩
```

## 책임 나누기

| 층 | 하는 일 | 모르는 것 |
|---|---|---|
| 저장소(`repo`) | 원격 · 로컬 중 어디서 가져올지(3초 시간 제한, 캐시 대체), 원본 → DTO 변환 | Retrofit · Room 호출 방법 |
| 원격 데이터 소스 | 서버 호출. 실패를 `safeApiCall` 로 `DomainResult` 로 바꿈 | DTO, 캐시 |
| 로컬 데이터 소스 | DB 읽기 · 쓰기, 캐시 보관 규칙(무엇을 얼마나 남길지) | DTO, 서버 |

- DTO 는 저장소부터 나타나고, `~Api` · `~Entity` 는 그 data 모듈 밖으로 나가지 않습니다.
- `~Api` 필드는 모두 nullable 이고 기본값이 없습니다. 서버가 보내지 않은 값은 null 그대로 화면까지 가고, 어떻게 보일지는 화면이 정합니다. 목록 · 총 개수 · 끝 여부가 없는 응답은 빈 결과가 아니라 에러입니다.
- 호출마다 try-catch 를 쓰지 않습니다. 서버는 `safeApiCall`, DB 는 `safeDbCall` · `safeDbFlow` 가 결과로 바꾸고, 취소는 실패로 보지 않고 다시 던집니다.

## 검색과 오프라인 (`SearchRepositoryImpl`)

```
서버 호출(3초 제한)
 ├─ 성공 → 결과를 저장하고 돌려줌
 └─ 실패 · 시간 초과
     ├─ 같은 검색의 저장 결과 있음 → 그것을 돌려줌(저장 시각 포함)
     ├─ 첫 페이지면 저장된 모든 책에서 검색어로 찾음 → 있으면 돌려줌(다음 페이지 없음)
     └─ 없음 → 원래 실패를 돌려줌
```

- 연결 상태를 미리 살피지 않습니다. 연결이 없으면 요청이 곧바로 실패하고, 느린 연결은 연결 상태로 알 수 없어 시간 제한이 어차피 필요합니다.
- 자동 재시도는 하지 않습니다. 사용자가 다시 시도를 누릅니다.

## DB (`:data:base` 의 `db/`)

| 테이블 | 내용 |
|---|---|
| `book` (`BookEntity`) | 책 정보. 한 권에 한 행. 상세 화면이 키로 읽는다 |
| `favorite` (`FavoriteEntity`) | 즐겨찾기한 책의 키와 저장 시각 |
| `search_cache` (`SearchCacheEntity`) | (검색어, 정렬, 페이지) 안의 책 순서와 페이지 정보 |

- Room 은 모든 테이블을 한 DB 클래스에서 알아야 해서 테이블 · DAO 를 `:data:base` 한곳에 모읍니다.
- 검색 캐시는 (검색어 · 정렬) 최근 20조합 × 5페이지를 남기고, 1페이지를 저장할 때 `favorite` 와 `search_cache` 어느 쪽도 가리키지 않는 책을 지웁니다(`IBookDao.deleteUnreferenced`). **책을 가리키는 테이블을 새로 만들면 이 쿼리의 조건에도 더해야** 그 책이 지워지지 않습니다.
- DB 는 아직 배포 전이라 `version = 1`, 마이그레이션이 없습니다. 첫 배포 뒤 테이블을 바꿀 때부터 마이그레이션을 더합니다.
- 책의 매칭 키는 ISBN13 → ISBN10 → (ISBN 없음) 제목 · 저자 · 출판사 순으로 만들어, 같은 책은 다시 검색해도 같은 키가 나옵니다.
- DAO 쿼리는 Robolectric 으로 JVM 에서 실행해 테스트합니다(`data/base/src/test`).

## 새 데이터 출처를 더할 때

1. 서버 API 면 `data/api/<경로>`, 기기 DB 만이면 `data/local/<대상>` 모듈을 만들고 `convention.data` 를 붙입니다.
2. 새 테이블이 필요하면 `:data:base` 의 `db/` 에 Entity · DAO 를 더하고 `AbsBookDatabase` 에 등록합니다.
3. 데이터 소스 → 저장소 순으로 만들고, `di/` 에서 `ViewModelComponent` 범위로 바인딩합니다.
4. `:app` 의 build.gradle.kts 에 모듈을 더합니다(Hilt 그래프).
