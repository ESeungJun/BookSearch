# 카카오 도서 검색 API — 계약과 실측

`GET https://dapi.kakao.com/v3/search/book`, 헤더 `Authorization: KakaoAK {REST API 키}`.
실측일 2026-10-09. 표본은 '코틀린' accuracy 1·2페이지, latest 1페이지(size 20), '해리포터' size 50으로 모두 110건이다. 첫 버전 작업 때 실호출로 확인했다. 키 값은 기록하지 않는다.

## 요청

| 파라미터 | 이 앱의 사용 | 실측 |
|---|---|---|
| `query` | 필수 | 빠지면 400이 아니라 **200 + 0건 + `is_end=false`**. 빈 값·공백·`*`·`%` 모두 0건 → 전체 목록 조회 불가 |
| `sort` | `accuracy`(정확도순) / `latest`(발간일순) | latest는 날짜 내림차순으로 실제 동작 |
| `page` | 1부터 | 마지막 페이지를 넘기면 오류가 아니라 **마지막 페이지를 반복**하고 `is_end=true` |
| `size` | 20 고정 | 상한 50. 51을 요청하면 50건으로 잘린다 |

## 응답
`meta{total_count, pageable_count, is_end}`, `documents[]{title, contents, url, isbn, datetime, authors[], publisher, translators[], price, sale_price, thumbnail, status}`

| 필드 | 실측 | 처리 |
|---|---|---|
| `total_count` / `pageable_count` | pageable 은 1000에서 잘린다 | 화면의 총 개수는 `total_count` |
| `isbn` | `"ISBN10 ISBN13"` 공백 결합 91건, ISBN13 단독 19건. ISBN10 단독·빈 값은 미관측(문서상 가능) | 매칭 키 규칙(아래) |
| `sale_price` | **-1이 19건** = 할인 없음 | `sale_price > 0`일 때만 할인가 |
| `price` | 0은 미관측 | 0 이하는 "가격 정보 없음" |
| `contents` | 약 250자 요약(110건 중 102건이 250~260자, 문장 중간에서 끊김). 빈 문자열 1건 | 끝에 "…", 전문은 `url`을 Custom Tab으로 |
| `datetime` | ISO-8601 `2019-01-12T00:00:00.000+09:00` | 날짜만 표시 |
| `title` | 최대 59자 | 카드에서는 줄 수 제한 |
| `authors` | 최대 4명. 빈 배열은 미관측(문서상 가능) | 2명까지, 넘으면 "외 N명" |
| `thumbnail`, `publisher` | 빈 값 미관측(문서상 가능) | 자리표시·대체 문구 |

- 같은 정렬의 1·2페이지 사이, 한 응답 안에서 매칭 키 중복은 0건이었다. 다만 끝을 넘긴 반복 응답 때문에 중복은 생길 수 있다.
- 응답 시간 0.05~0.17초.

## 오류
- 키 오류·헤더 없음: 401 `{"errorType":"AccessDeniedError","message":...}`. **`message`에 키 일부가 들어가므로 로그·화면에 원문을 남기지 않는다.**
- 429(할당량)·5xx는 미관측(문서 기반으로만 대응).

## 매칭 키 (작성자 결정, v1 D-12·D-13)
같은 책을 다시 검색했을 때 즐겨찾기 상태를 맞추고, 목록 key로도 쓴다.
1. ISBN13이 있으면 `ISBN13 + 제목 + 저자` (둘 다 있으면 ISBN13 — `추천안 기본 채택`)
2. ISBN10만 있으면 `ISBN10 + 제목 + 저자`
3. 둘 다 없으면 `제목 + 저자 + 출판사`
