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
:core:common-ui             ← BaseViewModel, UiState, UiIntent, CurrentActivityHolder,
                              HealthConnectLauncher(건강 데이터 설정 진입)
:core:designsystem          ← ModyTheme, 공용 Compose 컴포넌트, 색상/타이포, ModyCatalog
:core:navigation            ← Route 정의, NavigationHelper, NavigationEvent
:core:camera                ← 촬영/크롭 Compose 레이어(CameraX). record·challenge 가 공유
:core:domain                ← Repository 인터페이스, 도메인 모델, UseCase (비즈니스 로직)
:core:data                  ← Repository 구현체, DataSource 구현체
:core:network               ← Retrofit, OkHttp, API 인터페이스, Interceptor
:feature:auth               ← 로그인(카카오·구글, 심사용 히든 로그인)
:feature:onboarding         ← 약관·권한·프로필 온보딩
:feature:group              ← 그룹 생성/참여/초대
:feature:feed               ← 피드, 기록 상세
:feature:challenge          ← 챌린지 탭(연속 기록·걸음 수 챌린지)
:feature:record             ← 기록 작성(촬영)
:feature:notification       ← 알림함
:feature:mypage             ← 마이페이지, 알림 설정, 건강 연동 안내
```

모듈을 새로 추가/제거하면 `settings.gradle.kts` 와 이 표를 같이 고친다. 디렉터리만 남고
`include` 가 빠지면 빌드에서 조용히 사라진다(`core/health/` 가 그 상태로 남아 있었다).

### 의존성 방향 (절대 역방향 금지)

```
:app          → :presentation, :core:data, :core:common-ui, :core:domain, :core:navigation
:presentation → :feature:*, :core:common-ui, :core:designsystem, :core:navigation, :core:domain
:feature:*    → :core:common-ui, :core:designsystem, :core:navigation, :core:domain
                (record·challenge 만 추가로 :core:camera)
:core:camera  → :core:designsystem, :core:domain
:core:common-ui → :core:domain
:core:data    → :core:domain, :core:network
:core:network → :core:domain
:core:domain  → (아무것도 없음)
:core:designsystem, :core:navigation → (아무것도 없음)
```

Repository 구현 바인딩(`:core:data`)은 `:app` 에서만 조립한다 — feature 는 `:core:domain`
인터페이스만 본다.

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

### 릴리스 산출물 — 빌드 성공이 "맞다"는 뜻이 아니다

아래 셋은 전부 `BUILD SUCCESSFUL` 로 끝나고 Play 업로드/제출 단계에서야 막힌다.

- **`--no-daemon` 없이는 `VERSION_CODE` env 가 무시된다.** `app/build.gradle.kts` 가
  `System.getenv()` 로 읽는데 이건 **Gradle 데몬 프로세스**의 환경을 본다. Studio 가 띄워둔
  데몬을 재사용하면 커맨드라인 env 가 닿지 않아 조용히 기본값으로 빌드된다.
  ```bash
  VERSION_CODE=<N> VERSION_NAME=<X.Y.Z> ./gradlew --no-daemon :app:bundleRelease
  ```
- **광고 권한이 저절로 들어온다.** `firebase-analytics` → `play-services-measurement-api` 가
  광고 권한을 **여러 이름으로** 선언한다. `com.google.android.gms.permission.AD_ID` 하나만
  막아두면 `ACCESS_ADSERVICES_AD_ID` / `ACCESS_ADSERVICES_ATTRIBUTION` 이 그대로 병합돼
  "광고 ID 사용: 아니요" 제출이 막힌다. 셋 다 `tools:node="remove"` 로 제거돼 있다
  (`app/src/main/AndroidManifest.xml`). 라이브러리를 올린 뒤엔 다시 확인할 것.
- **versionCode 는 되돌릴 수 없다.** 기기/Play 모두 더 낮은 값 설치·업로드를 거부한다.
  올리기 전 Play Console 의 최고 versionCode 를 확인한다.

업로드 전 산출물을 직접 뜯어 확인한다. `app/build/intermediates/**` 는 믿지 마라 —
Studio 백그라운드 빌드가 나중에 덮어써서 AAB 내용과 어긋난다.
```bash
# 권한 (APK) — uses-permission 만 정확히 나온다. 이걸 1순위로 본다.
~/Library/Android/sdk/build-tools/36.0.0/aapt2 dump permissions \
  app/build/outputs/apk/release/app-release.apk

# AAB 는 매니페스트가 protobuf 라 문자열로 확인한다
unzip -p app/build/outputs/bundle/release/app-release.aab base/manifest/AndroidManifest.xml \
  | strings | grep -iE 'permission\.(AD_ID|ACCESS_ADSERVICES)|READ_STEPS'
```

AAB 쪽 grep 패턴에 `permission\.` 을 반드시 붙인다. 그냥 `adservices` 로 훑으면
`android.adservices.AD_SERVICES_CONFIG`(`<property>`)와 `android.ext.adservices`(라이브러리)가
같이 걸리는데 **둘 다 권한이 아니고 지울 필요도 없다.** 문제가 되는 건
`android.permission.ACCESS_ADSERVICES_*` 와 `com.google.android.gms.permission.AD_ID` 뿐이다.

**Play 자동 배포는 아직 없다.** `.github/workflows/cd.yml` 의 `playstore` 잡은 미구성 상태로
`exit 1` 한다 — `v1.2.3` 같은 릴리스 태그를 밀면 CI 가 실패한다. 동작하는 건 `-dev` 태그
(Firebase App Distribution, debug APK)뿐이고, Play 제출은 위 명령으로 로컬 AAB 를 말아
콘솔에 수동 업로드한다. CD 의 versionCode 는 `v*` 태그 개수로 매긴다(태그를 지우면 값이
되돌아가므로 배포한 태그는 지우지 않는다).

release 서명은 **Android Studio JBR 21** 로만 된다. Homebrew JDK 17 이면 keystore 비번을
틀렸다는 에러가 난다(PKCS12 non-ASCII 처리 차이). `~/.gradle/gradle.properties` 의
`org.gradle.java.home` 설정을 먼저 본다.

### Remote Config (Firebase `mody-df59e`, iOS 와 공용)

조건은 앱 ID 기준 4개(`iOS DEV` / `iOS PROD` / `Android DEV` / `Android PRD`)다.
**플랫폼별로 값이 갈리므로 조건값을 빠뜨리면 default 를 받는다** — 안드로이드가
`app_store_url`(애플 링크) 기본값을 받고 있던 게 그 사례다. 그래서 스토어 URL 은
`play_store_url`(Android) / `app_store_url`(iOS) 로 키를 나눴다. iOS 가 읽는 키를
rename 하면 iOS 가 깨지므로 **이름을 바꾸지 말고 추가**한다.

`guest_login_flag` + `review_login_password` 는 심사용 히든 로그인(로그인 화면 로고 20연타)을
연다. **심사 기간에만** PRD 를 켜고 끝나면 되돌린다. 콘솔 값 확인·게시는 CLI 로도 된다.
```bash
firebase remoteconfig:get --project mody-df59e -o rc.json
```

## 브랜치 / 병렬 개발 규칙

- **기능별 브랜치에서 개발**: `main`에 직접 커밋 금지. 새 작업은 항상 기능 브랜치를 파서 진행하고 PR로 머지한다. 브랜치명은 `feat/xxx`, `fix/xxx` 형태(작업 성격 + 짧은 요약).
- **스택 PR**: 연쇄 의존 작업은 이전 브랜치를 base로 쌓고(P1→P2→…), 낮은 번호부터 순서대로 머지한다. 하나 머지되면 GitHub이 다음 PR base를 `main`으로 자동 재타겟한다.
- **워크트리 분리 금지 — 본문 단일 체크아웃에서만 작업**: 예전엔 브랜치마다 `git worktree` 로
  폴더를 나눴는데, 사용자가 에디터에서 직접 머지·전환하는 흐름과 겹쳐 오히려 어긋남을 만들었다
  (PR #53 머지 후 push 커밋이 main 에서 누락 → cherry-pick + 충돌 수동 해소). 본문
  `/Users/jeongdoyun/Project/mody` 하나에서 브랜치 전환으로만 작업한다.
  ```bash
  git switch -c feat/xxx origin/main   # 새 작업은 항상 origin/main 기준
  ```
- **작업 시작 전 현재 브랜치를 확인한다**: 사용자가 에디터에서 브랜치를 바꿔둔 채일 수 있다.
  확인 없이 커밋하면 `main` 에 직접 올라간다(실제로 발생). 커밋 직후 `git branch --show-current`
  로 한 번 더 본다.
- **기존 PR 브랜치에 push 하기 전 머지 여부 확인**: `gh pr view N --json state`. 사용자가 작업
  중간에 수시로 머지한다. 머지된 브랜치에 이어 커밋하면 고아 커밋이 된다 — 새 브랜치로 뗀다.

## PR / 커밋 규칙

- **PR 본문**: `.github/PULL_REQUEST_TEMPLATE.md` 양식을 채워서 올린다 (작업 내용 / 변경 이유 / 주요 변경사항 / 스크린샷 / 리뷰 포인트 / 체크리스트 / 관련 이슈). base 브랜치는 `main`.
- **PR 올리기 전 1차 셀프 코드리뷰**: 변경 diff를 훑어 (1) 버그·부작용(특히 공용 토큰/컴포넌트 리네임이 다른 화면에 미치는 영향), (2) 아래 **아키텍처 적합성**, (3) **비동기·실패 경로**를 함께 점검하고, 발견 사항을 PR "리뷰어가 집중해서 봐줬으면 하는 부분"에 남긴다.

### 아키텍처 셀프 점검 체크리스트 (Clean Architecture + MVI)

- **의존성 방향**: 역방향 참조 없음. `:core:domain`은 아무 데도 의존 안 함. Screen/ViewModel이 `:core:data`·`:core:network` 구현체를 직접 참조하지 않고 `:core:domain`의 Repository 인터페이스만 본다.
- **레이어 분리**: Screen(@Composable)은 UI 렌더링만 — 비즈니스 로직·조건 분기·데이터 가공 없음. 로직은 ViewModel `processIntent()`에 있고, 데이터 접근은 Repository 경유.
- **MVI 단방향 흐름**: 상태 변경은 오직 `setState { copy(...) }`로만. State는 `data class : UiState`(immutable). Intent는 `sealed class : UiIntent`이고 사용자 액션은 전부 `onIntent(...)`로 진입 — Screen이 ViewModel 내부 메서드 직접 호출 금지.
- **화면 이동**: ViewModel에서 `navigationHelper.navigate(NavigationEvent.*)`로만. Screen에서 NavController 직접 조작 금지.
- **DI**: ViewModel `@HiltViewModel`+`@Inject`, Repository 구현체 `@Singleton`+`@Binds`, 모듈은 각 모듈 `di/` 패키지.
- **네이밍/구조**: 네이밍 컨벤션 표 준수, 새 기능은 `feature/xxx` 파일 구조(navigation·screen·viewmodel·contract) 따름.
- **커밋 단위**: 논리 단위로 분리 (designsystem 변경 / feature UI / fix 등 섞지 않기). 커밋 메시지는 `type(scope): 요약` (Conventional Commits).
- **커밋 트레일러**: 커밋 메시지 끝에 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`, PR 본문 끝에 `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.

### 비동기·실패 경로 셀프 점검

실제 코드리뷰에서 Major 로 반복해 걸린 유형들. 아키텍처 체크리스트를 통과해도 여기서 걸린다.

- **비슷한 문제를 이미 푼 코드가 레포에 있는지 먼저 찾는다.** 아래 항목 대부분은 이미 한 번
  풀린 문제였고, 새 코드가 그 선례를 안 따라서 다시 났다. 나머지 항목은 이걸 놓쳤을 때의 보험.
- **응답 도착 시 컨텍스트 재확인**: 요청을 시작할 때의 `groupId`/날짜/id 를 지역 변수로 붙들고,
  성공·실패 핸들러 **첫 줄**에서 현재 값과 같은지 본다. 다르면 버린다. 사용자가 그 사이 그룹·
  날짜를 바꾸면 늦게 온 응답이 엉뚱한 대상에 반영된다.
  선례: `FeedViewModel.loadMoreFeeds` — *"요청 시점 날짜 고정 — 조회 중 날짜 바뀌면 이어붙이기 취소"*
- **중복 실행 차단**: 같은 작업이 겹쳐 돌 수 있으면 진행 중 `Job`/`Set` 으로 막는다. 먼저 시작한
  쪽이 늦게 끝나면서 최신 결과를 덮어쓴다. 선례: `MainViewModel.stepSyncJob`
- **실패 경로에서 상태를 먼저 소비하지 않기**: `consume()`·플래그 set 처럼 되돌릴 수 없는 소비는
  후속 동작이 **성공한 뒤**에. 중간에 실패하면 재시도할 수 있는 상태로 남겨둔다.
- **기본값이 정보를 지우지 않는지**: DTO 기본값(`= false`)과 폴백 로직이 겹치면 "필드 없음"과
  "명시적 false"가 뭉개진다. 둘을 구분해야 하면 nullable 로 두고 `?:` 로 채운다.
- **경계값 명시**: 네트워크 호출엔 전체 호출 타임아웃(OkHttp 는 `readTimeout` 만으론 부족 —
  `callTimeout`), 목록엔 상한, 재시도엔 횟수.
- **`runCatching` 은 `CancellationException` 까지 삼킨다**: ViewModel 의 suspend 호출은
  `try/catch (e: CancellationException) { throw e }` 로. 안 그러면 화면을 떠난 뒤에도
  후속 코드가 계속 돌아 `setState` 를 때린다.
- **실패를 조용히 넘기지 않기**: 조회 실패를 빈 목록으로 흡수하면 "데이터 없음"과 구분되지 않는다.
  등록·삭제 실패를 삼키면 사용자가 성공한 줄 알고 화면을 닫는다.

## 시안대로 화면 짜기

**절차는 `figma-spec-first` 스킬(`.claude/skills/figma-spec-first/`)을 따른다.** 화면을
새로 짜거나 간격을 고칠 때 그 스킬을 먼저 읽는다. 스킬은 프로젝트 무관한 절차만 담고,
여기는 모디에만 해당하는 값들이다.

### 시안 파일

```
file key: uQWUtLv8xzOFNrthwozXs9
https://figma.com/design/uQWUtLv8xzOFNrthwozXs9/모디-MODY--export용-
```

구 파일 `eUXrUuSsupAVdKb5xIatWW` 는 폐기됐다(PPT 페이지만 남음). `9:29`("UI" 캔버스)에
`get_metadata` 를 통째로 부르면 응답이 감당이 안 되니 화면 단위 노드로 좁힌다.

자주 쓴 노드: `251:3651`(걸음 수) · `251:2309`(챌린지 탭) · `251:3238`(연속 기록 탭) ·
`251:3880`(주간 챌린지 상세) · `512:4543`(공용 chip)

### Figma 변수명 → 코드 토큰

이름이 서로 다르다. 숫자를 직접 쓰지 말고 이 표로 옮긴다.

| Figma | 코드 |
|---|---|
| `Main0` / `Main` / `Main3` / `Main4` | `ModyColors.primary0` / `primary100` / `primary300` / `primary400` |
| `Sub` | `ModyColors.secondary100` |
| `Gray1` ~ `Gray10` | `ModyColors.gray01` ~ `gray10` (한 자리는 0 패딩) |
| `H1_Bold` / `H3_Bold` | `ModyTypography.h1` / `h3` |
| `B3_SemiBold` / `B5_Bold` / `B6_SemiBold` | `ModyTypography.b3` / `b5` / `b6` |
| `C1_Semibold` / `C2_Medium` / `C3_SemiBold` | `ModyTypography.c1` / `c2` / `c3` |

전체 매핑은 `tools/figma/figma-to-code-mapping.json`.

### 토큰 검사

```bash
python3 tools/figma/check_design_tokens.py
```

`tools/figma/figma-tokens.lock.json`(시안 스냅샷)과 `Color.kt` / `Type.kt` 를 대조한다.
Figma 를 호출하지 않아 인증키가 필요 없다. 시안이 바뀌면 사람이 lock 을 갱신한다 —
절차는 `tools/figma/README.md`. CI(`.github/workflows/ci.yml`)가 PR 마다 돌린다.

**초록불을 "시안대로다"로 읽으면 안 된다.** 이 검사는 토큰 *선언부*만 본다. `gray07`
자리에 `gray08` 을 써도 통과하고, 간격은 아예 범위 밖이다. 실제로 이 CI 를 붙인 다음 날
간격 14건이 틀렸다. 간격은 스킬의 스펙 시트로만 잡힌다.

모디 시안은 타이포가 전부 `lineHeight = fontSize × 1.4` 다. 이 규칙은 lock 에 없는
토큰까지 전부 적용된다.

### 반복해서 났던 실수

- **`24` 를 손이 먼저 친다.** 정정 18건 중 9건. 특히 가로 패딩 — 한 화면에 24 와 36 이
  같이 있다(연속 기록 탭 상단 36, 버디 섹션 24).
- **간격 값이 정수가 아니다.** `37`, `27.7`, `22.5`, `35.5`. 반올림하면 어긋난다.
  그래서 스페이싱 토큰을 만들 수 없고 실측 + 근거 주석이 유일한 답이다.
- **공용 컴포넌트가 없어 같은 걸 두 번 그렸다.** 시안 `chip`(512:4543)이 "완료" 배지와
  D-N 칩 두 곳에 쓰이는데 코드엔 공용 칩이 없어 규격이 갈라졌다. `ModyChip` 으로 추출했다
  (`core/designsystem/.../component/ModyChip.kt`). 칩 모양이 필요하면 새로 그리지 말고
  이걸 쓴다 — `RoundedCornerShape(100.dp)` 가 아직 7곳에 남아 있어 또 갈라질 수 있다.

### 공용 컴포넌트 카탈로그

`core/designsystem/.../catalog/ModyCatalog.kt` 의 `@Preview` 를 Android Studio 에서 열면
공용 컴포넌트를 한눈에 볼 수 있다. **새로 그리기 전에 여기부터 본다.**

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
| 스플래시 게이트·딥링크 | `presentation/.../MainViewModel.kt`, `MainActivity.kt` |
| 하단 탭 구성 | `presentation/.../main/MainScreenViewModel.kt` |
| Remote Config | `core/data/.../repository/RemoteConfigRepositoryImpl.kt` |
| 걸음 수(Health Connect) | `core/data/.../repository/HealthRepositoryImpl.kt`, `core/domain/.../usecase/SyncTodayStepsUseCase.kt` |
| 매니페스트(권한 선언) | `app/src/main/AndroidManifest.xml`, `presentation/src/main/AndroidManifest.xml` |
| CI / CD | `.github/workflows/ci.yml`, `cd.yml` |
