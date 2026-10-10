# AI 단독 결정 — 작성자 리뷰용

AI 가 작성자 확인 없이 정한 것(`추천안 기본 채택`)을 모은다. 단계 리뷰 때 작성자와 함께 보고, 확정·변경되면 `decision-log.md` 1절에 반영하고 여기서 지운다. 번호는 매기지 않는다.

| 단계 | 주제 | 선택지 (**고른 것**) | 이유 | 다르게 하면 | 위치 |
|---|---|---|---|---|---|
| v1 이월 | 두 ISBN 이 모두 있을 때 매칭 키 | ISBN10 / **ISBN13** | 13자리가 현행 표준이고 실측 110건 모두 ISBN13 을 가짐 | ISBN10 우선이면 13 단독 책과 키 체계가 갈림 | `docs/api.md` |
| 2단계 | 오프라인 판단 | 연결 상태 관찰 / **요청 실패(3초 초과·IOException)로만** | data 가 Context 를 들지 않는다(v1 지적 3). 느린 연결은 연결 상태로 알 수 없다 | 연결 복귀 시 자동 재요청은 없다 | `SearchRepositoryImpl.searchBooks` |
| 2단계 | 캐시 LRU 기준 | 마지막 사용 / **마지막 저장 시각** | 저장할 때만 순서를 갱신하면 읽기 경로에 쓰기가 없다 | 캐시로 다시 본 조합도 순서가 오르지 않는다 | `ISearchCacheDao.deleteOldCombinations` |
| 2단계 | 0건 결과 캐시 | 저장 / **저장 안 함** | 남길 책이 없다 | 오프라인에서 0건 검색어는 '결과 없음' 대신 오류로 보인다 | `SearchLocalDataSourceImpl.saveSearchPage` |
| 2단계 | 6페이지 이후 책 정보 | 저장 안 함 / **책 정보만 저장, 캐시 목록엔 안 남김** | 상세가 키로 찾을 수 있어야 한다 | 다음 새 검색 때 정리되므로 그 뒤 프로세스 재시작 시 상세를 못 찾을 수 있다 | `SearchLocalDataSourceImpl.saveSearchPage`, `IBookDao.deleteUnreferenced` |
| 2단계 | 즐겨찾기 검색 범위 | 제목만 / **제목 + 저자** | 원문 '질의어 기반 로컬 검색'에 저자 검색도 자연스럽다 | — | `IFavoriteDao.observe` |
| 2단계 | 마지막 검색 저장 위치 | 별도 테이블·DataStore / **검색 캐시의 가장 최근 저장 행** | 저장할 곳을 하나 더 만들지 않는다 | 0건·실패한 검색은 마지막 검색이 되지 않는다(결과를 받은 검색만) | `ISearchCacheDao.getLatest` |
| 이름 규칙 | Hilt `@Binds` 모듈(인터페이스) 이름 | 규칙 예외로 그대로 / **`I~Module`**(`IDetailModule` 등) | '인터페이스는 항상 `I~`'를 예외 없이 따른다 | 예외를 두면 규칙을 외울 것이 늘어난다 | `:di` |
| 이름 규칙 | 테스트 대역 이름 | `~Impl` / **`Fake~` 유지**(`FakeSearchBookService` 등) | `~Impl` 은 앱의 실제 구현체만 가리키게 둔다. 테스트 안에서만 쓰는 private 클래스다 | 테스트 대역도 `~Impl` 이면 실제 구현과 구분이 안 된다 | `:data`·`:domain` 테스트 |
| 이름 규칙 | 변환 함수 이름 | `toBookDTO()` / **`toBook()` 유지** | 함수 이름은 무엇으로 바꾸는지만 말하면 충분하다 | — | `SearchBookMapper`, `BookEntity` |
| data 재구성 | HTTP 오류를 domain 결과로 바꾸는 자리 (`DomainResult`) | 저장소 / **원격 데이터 소스** | 저장소가 Retrofit(`HttpException`)을 몰라도 된다 | — | `SearchRemoteDataSourceImpl` |
| data 재구성 | 요청 크기(20)·정렬 값 변환·캐시 보관 수 위치 | 저장소 / **각 데이터 소스** | 페이지 크기·정렬 문자열은 API 사정, 5페이지·20조합은 저장 사정이다. 시간 제한 3초만 저장소에 둔다 | — | 각 `~Impl` 의 `companion object` |
| 골격 | 모듈 공통 빌드 설정 방식 | 모듈마다 직접 적기 / 루트 allprojects·subprojects / buildSrc / **포함 빌드(build-logic)의 컨벤션 플러그인** | 바꿀 곳이 한 파일, 모듈 파일엔 레이어 플러그인 한 줄. allprojects 는 무엇이 적용되는지 모듈에서 안 보이고, buildSrc 는 바뀌면 모든 빌드 스크립트가 다시 컴파일된다 | 플러그인 코드를 읽어야 모듈 설정 전체가 보인다 | `build-logic/convention` |
