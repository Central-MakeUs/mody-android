# 대규모 멀티모듈 아키텍처 이관 플랜

mody 멀티모듈 구조를 **대규모 확장을 전제로 한 레퍼런스 아키텍처**(fine-grained compile-avoidance)로
리팩터링하고, `.claude` 기반 규칙 강제(hook)·스킬·에이전트를 함께 도입하기 위한 분석 및 단계 플랜 문서.

- 대상 레포: mody
- 설계 기준: 모듈 경계·빌드 격리(compile avoidance)를 우선하는 대규모 멀티모듈 아키텍처
- 결정 사항: **① 구조 전면 리팩터링** / **② 영역 분리 단위는 repository당 1영역**

---

## 1. 현재 구조 (mody)

```
:app                    → :presentation, :core:data, :core:domain, :core:common-ui, :core:navigation (implementation)
:presentation           → :feature:* 8개 전부 + core:designsystem/navigation/common-ui/domain (implementation)
:feature:<기능>          → (convention) core:common-ui, designsystem, navigation, domain   ※ 단일 모듈
:core:domain            → :core:model (api)                              ※ 단일 monolith, 순수 Kotlin(jvm.library)
:core:data              → :core:domain, :core:network                   ※ 단일 monolith
:core:network           → (인프라)
:core:model             → (순수 Kotlin)
:core:common-ui / designsystem / navigation / camera
```

- feature 8개: auth, onboarding, group, feed, challenge, record, notification, mypage
- `core:domain`의 repository 인터페이스 13개:
  Auth, Challenge, Feed, Group, Health, ImageShare, ImageUpload, MyPage,
  Notification, Onboarding, PushToken, Record, RemoteConfig
  (+ Session/SocialLogin/SessionReauthenticator 등 보조 계약)

### 이미 잘 지켜지는 것 (목표 핵심 규칙과 동일)

| 원칙 | 현재 mody | 비고 |
|---|---|---|
| domain 순수 Kotlin | ✅ `mody.jvm.library` | 규칙 3 충족 |
| repository 인터페이스=domain, 구현=data | ✅ `core:data → core:domain` | 규칙 1 충족 |
| feature가 data/network 못 봄 | ✅ **convention plugin이 목록에서 제외** | 규칙 2 충족 |
| feature끼리 의존 안 함 | ✅ presentation이 취합 | 규칙 6과 사실상 동일 |

> 결론: mody는 "pure domain + data 경계 격리"라는 **어려운 부분을 이미 convention plugin으로 강제**하고 있다.
> 리팩터링은 재설계가 아니라 **compile-avoidance 극대화**를 위한 세분화다.

---

## 2. 목표 구조 (대규모 멀티모듈)

```
:app                         → core:data:*, feature:*:impl 을 runtimeOnly 로만 의존 (Hilt 가 런타임 조립)
:presentation                → feature:*:api 의존(Route 계약) + feature:*:impl 은 runtimeOnly
:feature:<기능>:api           → Route(Graph) 계약 + NavigationEntry 만 (core:navigation 만 의존)
:feature:<기능>:impl          → 화면/ViewModel/contract → core:domain:<영역>, 다른 feature 의 api 만 의존
:core:domain:<영역>           → 순수 Kotlin: model, repository interface, usecase   (영역당 1모듈)
:core:data:<영역>             → RepositoryImpl, DataSource, DTO, mapper, DI (internal)
:core:network                → 인프라 (Retrofit/OkHttp/Interceptor)
:core:model / common-ui / designsystem / navigation / camera
```

의존 방향: `feature:*:impl → core:domain:* ← core:data:* → core:network`

### 목표 구조가 추가로 가진 것 = compile-avoidance 3축

1. **feature api/impl 분리 + presentation이 impl을 runtimeOnly**
   → 현재는 `presentation`이 feature 8개를 `implementation`으로 봐서, 화면 한 줄 수정 시
   presentation·app까지 재컴파일. 분리하면 impl 변경이 다운스트림을 재컴파일시키지 않음. **효과 최대.**
2. **app이 `core:data`를 `runtimeOnly`**
   → 현재 `implementation`이라 data 변경 시 app 재컴파일. runtimeOnly면 컴파일 classpath에서 구현 은닉.
3. **domain/data 영역 분리**
   → 현재 단일 monolith라 data 한 곳 수정 시 data 전체 재컴파일. 영역별로 쪼개면 격리.

---

## 3. 영역(area) 분리 설계 — repository당 1영역

repository 인터페이스 1개 = `core:domain:<영역>` + `core:data:<영역>` 1쌍.

| 영역 | domain 모듈 | 주요 repository / 계약 |
|---|---|---|
| auth | `:core:domain:auth` | AuthRepository, SocialLoginProvider |
| session | `:core:domain:session` | SessionRepository, SessionReauthenticator, SessionExpiredNotifier |
| onboarding | `:core:domain:onboarding` | OnboardingRepository |
| feed | `:core:domain:feed` | FeedRepository |
| group | `:core:domain:group` | GroupRepository |
| challenge | `:core:domain:challenge` | ChallengeRepository |
| record | `:core:domain:record` | RecordRepository |
| notification | `:core:domain:notification` | NotificationRepository, PushTokenRepository, DeepLink/Store 계약 |
| mypage | `:core:domain:mypage` | MyPageRepository |
| health | `:core:domain:health` | HealthRepository, SyncTodaySteps/StepSyncWindow usecase |
| image | `:core:domain:image` | ImageShareRepository, ImageUploadRepository |
| remoteconfig | `:core:domain:remoteconfig` | RemoteConfigRepository |

- 각 영역에 대응하는 `:core:data:<영역>` 1개씩.
- **영역 간 조합은 data가 아니라 domain usecase에서** 각 영역의 repository를 받아 수행
  (규칙 4: `core:data:<영역>`은 다른 data 영역을 의존하지 않음).
- 공유 모델은 `:core:model`에 유지. domain 영역은 `:core:model`만 공유 의존.

> ⚠️ 트레이드오프: 13 domain + 13 data = 모듈 26개 증가. Gradle 설정·DI 보일러플레이트 증가.
> compile 격리 이득이 분명한 대신 모듈 관리 비용이 커진다. 측정(Stage별 verify)으로 이득을 확인하며 진행.

---

## 4. 단계 플랜

**핵심 순서 원칙: 의존 규칙 강제 hook은 맨 마지막에 "차단 모드"로 켠다.**
먼저 켜면 현재 구조가 전부 위반 판정 → 모든 편집 차단. 마이그레이션 동안은 **warn 모드**(출력만, exit 0).

각 Stage는 **빌드가 되는 상태**로 끝내고 `:app:assembleDebug`로 Hilt 그래프까지 검증한 뒤 커밋한다.
커밋은 논리 단위로 잘게. 형식 `<type>: <한국어 요약>`.

### Stage 0 — `.claude` 인프라 도입 (코드 변경 없음)
규칙 강제·스킬·에이전트를 mody에 맞춰 작성. **mody 타깃 구조 + `com.makeus.mody.*` 패키지** 기준.

- `CLAUDE.md` — 기존 mody 문서(Clean Arch + MVI, 템플릿) + 모듈 지도/절대 규칙 병합
- `rules/` — domain / data / feature / infra / architecture / testing (경로 트리거)
- `scripts/check_module_deps.py` — mody용 튜닝(아래 5장), **warn 모드로 시작**
- `scripts/format-kotlin.sh`
- `skills/` — new-feature(api/impl용), new-domain-area, add-api-endpoint, staged-implementation, verify-build-avoidance, commit
  (기존 `figma-spec-first` 스킬 유지)
- `agents/` — architecture-reviewer, test-writer
- `settings.json` — hooks(Pre/Post/Stop) + permissions

### Stage 1 — app `core:data` → `runtimeOnly` (가장 싸고 안전)
- app이 data 타입을 직접 참조하지 않는지 확인(현재 주석상 domain 타입만 참조).
- `implementation(project(":core:data"))` → `runtimeOnly(...)` 전환.
- 검증: `:app:assembleDebug` (Hilt 바인딩 유지 확인).

### Stage 2 — feature api/impl 분리 (8개, 하나씩)
feature당:
- `:feature:<f>:api` 신설 — Route/Graph 계약 + `NavigationEntry`(composable 등록 진입점)만. `core:navigation`만 의존.
- 기존 화면/ViewModel/contract/component → `:feature:<f>:impl`로 이동.
- `presentation`은 `:feature:<f>:api`를 `implementation`, `:feature:<f>:impl`을 `runtimeOnly`.
- convention plugin 추가: `mody.android.feature.api`, `mody.android.feature.impl`.
- **feature 하나 끝낼 때마다 `:app:assembleDebug`.**

처리 순서 제안(의존 적은 것부터): mypage → notification → challenge → record → feed → group → onboarding → auth.

### Stage 3 — domain/data 영역 분리 (영역 하나씩)
- `:core:domain` → `:core:domain:<영역>`, `:core:data` → `:core:data:<영역>`로 이동.
- 영역 간 조합 코드는 domain usecase로 이동(data 간 의존 제거).
- feature impl은 해당 영역 domain만 의존하도록 정리.
- **영역 하나 끝낼 때마다 `:app:assembleDebug`.**

처리 순서 제안(피의존 많은 공통부터): model 고정 → session/auth → 나머지 도메인.

### Stage 4 — hook 차단 모드 전환 + 전체 검증
- `check_module_deps.py`를 exit 2(차단) 모드로 전환(아래 5장 토글).
- `python3 .claude/scripts/check_module_deps.py all` 위반 0 확인.
- `:app:assembleDebug` + `testDebugUnitTest` 통과.
- `architecture-reviewer` 에이전트로 최종 경계 점검.

---

## 5. hook(check_module_deps.py) 튜닝 포인트

규칙 검사 스크립트는 `:core:domain:<영역>` / `:core:data:<영역>` / `:feature:…:api|impl` / `:app` 패턴에
범용적으로 동작하도록 작성한다. mody 기준으로 맞출 지점:

1. **금지 import 패키지**: `com.makeus.mody.core.data/network/datastore/database` 접두사를
   domain 코드에서 import 금지(`DOMAIN_FORBIDDEN_IMPORTS`).
2. **인프라 모듈 집합**: mody는 DB/DataStore 분리 모듈이 없음 → `is_infra`를 `:core:network` 중심으로.
   (datastore/security-crypto는 현재 `core:data`가 직접 사용 → 추후 정책 결정)
3. **feature api 허용 의존**: `(:core:navigation, :core:common-ui)`.
4. **domain 공유 의존 허용**: `:core:model`(+ 필요 시 공용 유틸)만 허용.
5. **warn/차단 토글**: 마이그레이션 동안 비차단. 예) `.claude/scripts/.enforce` 파일 존재 시에만 exit 2,
   없으면 stderr 출력 후 exit 0. Stage 4에서 `.enforce` 생성.

### 강제되는 규칙(도입 후)
1. repository 인터페이스는 `core:domain:<영역>`, 구현은 `core:data:<영역>`.
2. feature는 `core:domain:*`만 본다. `core:data:*`·인프라를 의존하지 않는다.
3. `core:domain:*`은 순수 Kotlin. Android/Retrofit/Hilt-android 타입 금지.
4. `core:data:<영역>`은 다른 `core:data:*`를 의존하지 않는다(조합은 domain usecase).
5. app은 `core:data:*`·`feature:*:impl`을 `runtimeOnly`로만 의존.
6. feature impl끼리 의존 금지. 타 feature 이동은 그 feature의 api만 사용.

---

## 6. 리스크 / 체크리스트

- **Hilt 바인딩 누락·중복은 app 빌드에서만 드러남** → 모듈 구조 변경 뒤 반드시 `:app:assembleDebug`.
- **영역 26모듈**: 과세분화 비용. Stage 3 진행 중 `verify-build-avoidance`로 실제 재컴파일 감소를 측정해
  이득이 비용을 넘는지 확인(안 넘으면 영역 묶음 재고).
- **convention plugin(build-logic)**: feature.api/impl, domain-area 규약 플러그인 신설 필요.
- **presentation 역할 축소**: 현재 feature 전부를 implementation으로 취합 → api 의존 + impl runtimeOnly로 전환.
  NavHost가 api의 `NavigationEntry`로 화면을 조립하도록 설계 변경.
- **네이밍/패키지**: 전 과정 `com.makeus.mody.*` 유지.
- 각 커밋은 빌드 가능 상태. 무관한 리팩터링 섞지 않기.

---

## 부록 A — 현재 vs 목표 요약표

| 항목 | 현재 mody | 목표 |
|---|---|---|
| domain 순수성 | ✅ 단일 jvm.library | ✅ 영역별 jvm.library |
| data 경계 격리 | ✅ (convention) | ✅ 영역별 internal |
| feature 구조 | 단일 모듈 | api + impl 분리 |
| feature↔다운스트림 컴파일 | presentation이 impl 직접 참조 | api 의존 + impl runtimeOnly |
| app↔data | implementation | runtimeOnly |
| domain/data 세분화 | monolith 1쌍 | 영역당 1쌍(13쌍) |
| 규칙 강제 | convention plugin(부분) | convention plugin + hook(전면) |
