# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Setup

Credentials are read from `local.properties` (not committed). Before building, add:

```
SUPABASE_URL=https://<project>.supabase.co
SUPABASE_KEY=<anon-key>
```

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.eous.mentor.SomeTest"

# Run lint
./gradlew lint
```

## Architecture

Single-activity app (`MainActivity`) with Jetpack Compose, Material3, and Navigation Compose.

### Dependency Injection

No Hilt/Dagger. `di/RepositoryProvider.kt` is a plain object service locator that lazily provides singleton repository instances. ViewModels receive repositories via constructor default arguments (e.g. `chatRepository: ChatRepository = RepositoryProvider.chatRepository`), keeping them testable.

The global Supabase client is a top-level val in `di/SupabaseDi.kt` — imported directly wherever needed.

### Navigation (two-level)

**Outer layer** — `core/navigation/AppNavigation.kt` (`AuthRouter`): a standard `NavHost` handling auth flow: `splash → intro / login / register / relogin → dashboard`. It also pre-instantiates `HomeViewModel` and `ChatViewModel` scoped to the authenticated user so the dashboard loads without a second spinner.

**Inner layer** — `features/main/MainScreen.kt`: once on `"dashboard"`, screen switching is driven entirely by `MainScreenViewModel.navigateTo(screen)` updating a `StateFlow<String>`. This is **not** another `NavHost` — screens are rendered with `AnimatedContent` on the current route string. Back-stack is handled manually with `BackHandler`.

Use `NavController.navigateSafe()` (defined in `AppNavigation.kt`) when navigating to prevent double-tap issues.

### Feature structure

Each feature lives in `features/<name>/` and follows a consistent pattern:

- `<Feature>Screen.kt` — top-level `@Composable` that observes the ViewModel state
- `<Feature>ViewModel.kt` — extends `ViewModel`, exposes `StateFlow<XState>`, communicates only through the state + function calls
- `<Feature>State.kt` — plain `data class` holding all UI state for the screen
- `<Feature>Components.kt` (optional) — private/internal sub-composables

### Domain / Data split

- `domain/model/Models.kt` — all shared data classes (serializable, used across layers)
- `domain/repository/` — repository interfaces (the contract)
- `core/data/repository/` — `*RepositoryImpl` classes that call Supabase directly
- `domain/usecase/` — thin use-case wrappers (not universally adopted; some features call repository methods directly from the ViewModel)

### Supabase backend

The app talks to Supabase via:

- **Postgrest** — direct table queries (`sessions`, `messages`, `bookmarks`, `quizzes`, `profiles`)
- **Auth** — sign-in / sign-up / session management; session state is observed via `SessionRepositoryImpl` which wraps `supabase.auth.sessionStatus`
- **Storage** — user avatar and chat image uploads (bucket `chat-images`)
- **Edge Functions** — `ai-chat` function receives `AiChatRequest` (message + history + optional image URL + user context) and returns `AiChatResponse` (reply, subject classification, optional generated quiz)

### State management conventions

- ViewModels expose a single `StateFlow<XState>` and mutate it exclusively via `_state.update { ... }`.
- Optimistic UI updates are used for bookmark toggles (update state first, revert on failure).
- `ChatViewModel` maintains an in-memory `sessionMessagesCache: ConcurrentHashMap` to avoid re-fetching messages when switching sessions.
- `HomeViewModel` and `ChatViewModel` are created once in `AuthRouter` keyed to `activeUserId` and passed down to avoid re-creation on recomposition.

### Theme

Colors, typography, and the app's custom colour constants (`EousPurple`, etc.) live in `core/ui/theme/`.

# Project Rules - Eous AI Mentor Android Mobile App

## Codebase Standard Rule

- **Language**: All codebase files (Kotlin, TypeScript, XML, Gradle config, documentation, rules, comments, and commit messages) MUST be written in English.

## Compilation & Verification Rule

- **Mandatory Compile Verification**: After making any source code modifications (Kotlin, XML, Gradle config), the agent MUST automatically run the compilation check (`./gradlew compileDebugKotlin` or `./gradlew assembleDebug`) to ensure no compile errors occur, without waiting for a user request.

## Communication Rule

- **Language**: All answers and plans MUST be written in Vietnamese.
