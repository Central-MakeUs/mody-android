# MODY

친구들과 식사 및 운동을 칼로리 제약 없이 간편하게 기록하고 공유합니다.
’소셜넛지’ 개념을 활용한 소통과 챌린지를 통해 다이어트 습관을 지속하도록 돕습니다.

## Tech Stack

- Kotlin

- Jetpack Compose

- Hilt

- Coroutine / Flow

- Retrofit / OkHttp

- DataStore

## Architecture

MODY Android는 멀티모듈 기반으로 구성되어 있으며, Feature와 Core Layer를 분리하여 유지보수성과 확장성을 높였습니다.

- UI: Jetpack Compose

- Architecture: MVI

- Dependency Injection: Hilt

- Asynchronous: Coroutine / Flow

- Network: Retrofit / OkHttp

- Local Storage: DataStore

- Navigation: Type-safe Navigation + NavigationHelper

## Project Structure

```text

mody

├── app                 # Application, Hilt EntryPoint

├── presentation        # MainActivity, AppNavHost, 앱 진입 및 라우팅

├── core

│   ├── common-ui       # MVI Base(ViewModel, State, Intent, SideEffect)

│   ├── designsystem    # Theme, Typography, Components

│   ├── navigation      # Route, NavigationHelper

│   ├── domain          # Model, Repository, UseCase

│   ├── data            # Repository 구현, DataStore

│   └── network         # Retrofit, DTO, Interceptor, Authenticator

└── feature

    ├── auth

    ├── onboarding

    ├── group

    ├── feed

    ├── record

    └── notification

```

> 현재 Feature 모듈을 지속적으로 분리 및 확장하며 아키텍처를 개선하고 있습니다.
