# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew assembleDebug          # debug 빌드 (applicationId: com.makeus.mody.dev)
./gradlew assembleRelease        # release 빌드 (applicationId: com.makeus.mody)
./gradlew installDebug           # 기기에 설치
./gradlew test                   # unit test
./gradlew test --tests "com.makeus.mody.XxxTest"  # 특정 테스트
./gradlew connectedAndroidTest   # instrumented test (기기 필요)
./gradlew lint                   # lint
```

## 모듈 구조

```
:app                        ← ModyApplication(@HiltAndroidApp)만, 의존성 조립
:presentation               ← MainActivity, NavHost, 최상위 Compose 진입점
:core:common-ui             ← BaseViewModel, UiState, UiIntent
:core:designsystem          ← ModyTheme, 공용 Compose 컴포넌트, 색상/타이포
:core:navigation            ← Route 정의, NavigationHelper, NavigationEvent
:core:domain                ← Repository 인터페이스, 도메인 모델 (비즈니스 로직)
:core:data                  ← Repository 구현체, DataSource 구현체
:core:network               ← Retrofit, OkHttp, API 인터페이스, Interceptor
:feature:*                  ← 기능별 화면 (아직 없음, Phase 2에서 추가)
```

### 의존성 방향 (절대 역방향 금지)

```
:app → :presentation → :feature:* → :core:common-ui, :core:designsystem, :core:navigation, :core:domain
:core:data → :core:domain
:core:network → :core:domain
:core:domain → (아무것도 없음)
```

## 아키텍처: Clean Architecture + MVI

### MVI 흐름

```
Screen
  └─ viewModel.onIntent(XxxIntent.DoSomething)
       └─ processIntent() [ViewModel]
            └─ repository 호출 or 로직
                 └─ setState { copy(...) }
                      └─ StateFlow → Compose recompose
```

### 새 기능 추가 시 파일 구조

`:feature:auth`를 예로 들면:

```
feature/auth/
└── src/main/java/com/makeus/mody/feature/auth/
    ├── navigation/
    │   └── AuthNavigation.kt          ← NavGraphBuilder 확장함수
    ├── login/
    │   ├── LoginScreen.kt             ← @Composable, UI만
    │   ├── LoginViewModel.kt          ← @HiltViewModel, BaseViewModel 상속
    │   └── contract/
    │       ├── LoginState.kt          ← data class : UiState
    │       └── LoginIntent.kt         ← sealed class : UiIntent
    └── signup/
        ├── SignUpScreen.kt
        ├── SignUpViewModel.kt
        └── contract/
            ├── SignUpState.kt
            └── SignUpIntent.kt
```

### 코드 템플릿

**State + Intent:**
```kotlin
data class LoginState(
    val email: String = "",
    val isLoading: Boolean = false,
) : UiState

sealed class LoginIntent : UiIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data object LoginClicked : LoginIntent()
}
```

**ViewModel:**
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<LoginState, LoginIntent>(LoginState()) {

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> setState { copy(email = intent.email) }
            is LoginIntent.LoginClicked -> login()
        }
    }

    private fun login() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        // authRepository.login(...)
        setState { copy(isLoading = false) }
    }
}
```

**Screen:**
```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // UI 렌더링만, 로직 없음
    Button(onClick = { viewModel.onIntent(LoginIntent.LoginClicked) }) {
        Text("로그인")
    }
}
```

**Route 추가 (`core/navigation/Route.kt`):**
```kotlin
@Serializable data object AuthGraphRoute : Route

sealed interface AuthGraph : Route {
    @Serializable data object LoginRoute : AuthGraph
    @Serializable data object SignUpRoute : AuthGraph
}
```

**Navigation 등록 (`feature/auth/.../AuthNavigation.kt`):**
```kotlin
fun NavGraphBuilder.authNavGraph() {
    navigation<AuthGraphRoute>(startDestination = AuthGraph.LoginRoute) {
        composable<AuthGraph.LoginRoute> { LoginScreen() }
        composable<AuthGraph.SignUpRoute> { SignUpScreen() }
    }
}
```

**ViewModel에서 화면 이동:**
```kotlin
navigationHelper.navigate(NavigationEvent.To(AuthGraph.SignUpRoute))
navigationHelper.navigate(NavigationEvent.Up)
```

## DI 규칙 (Hilt)

- ViewModel: `@HiltViewModel` + `@Inject constructor`
- Repository 구현체: `@Singleton` + `@Inject constructor`, `@Binds`로 인터페이스에 바인딩
- DataSource: `@Singleton` + `@Inject constructor`
- DI 모듈 위치: 각 모듈의 `di/` 패키지 안 (`XxxModule.kt`)

```kotlin
// core/data/di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
```

## 네이밍 컨벤션

| 종류 | 패턴 | 예시 |
|---|---|---|
| Screen | `XxxScreen.kt` | `LoginScreen.kt` |
| ViewModel | `XxxViewModel.kt` | `LoginViewModel.kt` |
| State | `XxxState.kt` | `LoginState.kt` |
| Intent | `XxxIntent.kt` | `LoginIntent.kt` |
| Repository 인터페이스 | `XxxRepository.kt` | `AuthRepository.kt` |
| Repository 구현체 | `XxxRepositoryImpl.kt` | `AuthRepositoryImpl.kt` |
| DataSource 인터페이스 | `XxxDataSource.kt` | `AuthDataSource.kt` |
| DI 모듈 | `XxxModule.kt` | `DataModule.kt` |
| Navigation | `XxxNavigation.kt` | `AuthNavigation.kt` |

## 빌드 변형

| buildType | applicationId | 용도 |
|---|---|---|
| debug | `com.makeus.mody.dev` | 개발 (폰에 동시 설치 가능) |
| release | `com.makeus.mody` | 배포 |

환경별 URL 등 BuildConfig 값은 각 모듈의 `build.gradle.kts` `buildTypes` 블록에 추가:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.mody.makeus.in/\"")
```

## PR / 커밋 규칙

- **PR 본문**: `.github/PULL_REQUEST_TEMPLATE.md` 양식을 채워서 올린다 (작업 내용 / 변경 이유 / 주요 변경사항 / 스크린샷 / 리뷰 포인트 / 체크리스트 / 관련 이슈). base 브랜치는 `main`.
- **PR 올리기 전 1차 셀프 코드리뷰**: 변경 diff를 훑어 버그·부작용(특히 공용 토큰/컴포넌트 리네임이 다른 화면에 미치는 영향)을 먼저 점검하고, 발견 사항을 PR "리뷰어가 집중해서 봐줬으면 하는 부분"에 남긴다.
- **커밋 단위**: 논리 단위로 분리 (designsystem 변경 / feature UI / fix 등 섞지 않기). 커밋 메시지는 `type(scope): 요약` (Conventional Commits).
- **커밋 트레일러**: 커밋 메시지 끝에 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`, PR 본문 끝에 `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.

## 핵심 파일 위치

| 역할 | 경로 |
|---|---|
| BaseViewModel | `core/common-ui/.../base/BaseViewModel.kt` |
| Route 정의 | `core/navigation/.../Route.kt` |
| NavigationHelper | `core/navigation/.../NavigationHelper.kt` |
| ModyTheme | `core/designsystem/.../theme/Theme.kt` |
| 색상 토큰 | `core/designsystem/.../theme/Color.kt` |
| 타이포 | `core/designsystem/.../theme/Type.kt` |
| NavHost | `presentation/.../navigation/AppNavHost.kt` |
| Hilt 진입점 | `app/.../ModyApplication.kt` |
