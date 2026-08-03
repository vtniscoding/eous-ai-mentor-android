# Project Rules - Eous AI Mentor Android Mobile App

## Codebase Standard Rule
- **Language**: All codebase files (Kotlin, TypeScript, XML, Gradle config, documentation, rules, comments, and commit messages) MUST be written in English.

## Compilation & Verification Rule
- **Mandatory Compile Verification**: After making any source code modifications (Kotlin, XML, Gradle config), the agent MUST automatically run the compilation check (`./gradlew compileDebugKotlin` or `./gradlew assembleDebug`) to ensure no compile errors occur, without waiting for a user request.

