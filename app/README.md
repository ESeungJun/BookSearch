# :app — 앱 골격

앱의 유일한 Activity 와 탭 골격, 화면 이동의 실제 구현을 둡니다. 기능 화면의 내용은 모르고, 각 기능 모듈이 내놓은 화면 등록을 모아 그리기만 합니다.

## 파일

| 파일 | 하는 일 |
|---|---|
| `MainActivity` | 유일한 Activity. 넓은 창에서 목록과 상세를 한 화면에 두려면 두 화면이 같은 Compose 트리에 있어야 해서 Activity 를 나누지 않는다 |
| `MainScaffold` | 탭 골격. 창 너비에 따라 하단 탭바 ↔ 측면 레일(`NavigationSuiteScaffold`). 탭별 백스택을 만들고, 키보드 · 시스템 내비 바 여백을 준다 |
| `navigation/AppNavHost` | 모든 탭의 백스택을 한 `NavDisplay` 로 그린다. 화면 등록(`Set<EntryProviderInstaller>`)을 모아 넘긴다 |
| `navigation/AppNavigator` | `INavigator` 구현. 지금 탭의 백스택에 이동을 전한다 |
| `navigation/AdaptiveLayout` | 600dp 이상 목록-상세 2칸 설정, 화면 전환 애니메이션 |
| `navigation/CurrentTabSceneStrategy` | 뒤로 갈 곳을 지금 탭 안으로 한정한다 |
| `di/IAppNavigatorModule` | `AppNavigator` → `INavigator` 바인딩 |

## 동작

**탭과 백스택**
- 탭(검색 · 즐겨찾기)마다 `rememberNavBackStack` 으로 백스택을 따로 둡니다. 백스택은 화면 회전 · 프로세스 재시작 뒤에도 복원됩니다.
- `NavDisplay` 에는 모든 탭의 항목을 넘기고 지금 탭의 항목을 맨 뒤에 둡니다. 목록에서 빠진 항목은 닫힌 화면으로 처리되어 ViewModel 과 입력 상태가 지워지므로, 다른 탭 항목도 남겨 두는 것입니다.
- 같은 화면이 두 탭에 있어도 키가 겹치지 않게 항목을 `TabRoute(탭, 경로)` 로 감쌉니다.

**뒤로 가기**

| 상황 | 결과 |
|---|---|
| 지금 탭에 이전 화면이 있음 | 그 화면으로(2칸이면 상세만 닫힘) |
| 즐겨찾기 탭 첫 화면 | 검색 탭으로 |
| 검색 탭 첫 화면 | 시스템에 맡김(홈으로) |

`NavDisplay` 는 앞에 다른 탭 항목이 있으면 첫 화면에서도 뒤로 가기를 받아 미리보기에 다른 탭을 보입니다. `CurrentTabSceneStrategy` 가 이를 막습니다.

**창 크기**
- 600dp 미만: 한 칸. 상세는 목록 위로 쌓이고 좌우로 밀려 들어옵니다.
- 600dp 이상: 목록 + 상세 2칸. 2칸에서 다른 책을 누르면 상세를 쌓지 않고 바꿉니다(`DetailRouterImpl`).
- 키보드가 떠 있는 동안 하단 탭바를 숨깁니다. 탭바가 남으면 키보드 여백이 탭바 높이만큼 더 생기기 때문입니다.

## 새 기능을 더할 때

1. `app/build.gradle.kts` 에 새 기능 main 모듈(과 Hilt 바인딩이 있는 data 모듈)을 더합니다. Hilt 가 `:app` 에서 그래프를 만들기 때문입니다.
2. 새 탭이 필요하면 `MainScaffold` 의 `Tab` 과 백스택 맵에 더합니다. 탭이 아닌 화면은 `:app` 을 고칠 필요가 없습니다.
