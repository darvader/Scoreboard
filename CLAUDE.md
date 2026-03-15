# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.darvader.scoreboard.matrix.LedMatrixTest"

# Run instrumented/UI tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Install debug APK on device
./gradlew installDebug
```

## Project Overview

Android app (Kotlin) that controls LED matrix scoreboard displays over UDP. It can also pull live volleyball scores from SAMS ticker servers (TVV/DVV regions) via WebSocket and automatically update the LED matrices.

**Single-module Gradle project** (`app/`) targeting API 26+ (compileSdk 35), using Java 11, Kotlin 2.0.21, and ViewBinding.

## Architecture

```
com.darvader.scoreboard/
├── MainActivity.kt          # Entry point, matrix discovery via UDP broadcast
├── EchoClient.kt            # UDP client (IEchoClient interface for testability)
├── EchoServer.kt            # UDP server listening for matrix discovery responses
└── matrix/
    ├── LedMatrix.kt          # Core score state & LED command logic
    ├── activity/
    │   ├── ScoreboardActivity.kt   # Manual score control UI
    │   └── LiveScoreActivity.kt    # Live score selection/display UI
    └── livescore/
        ├── Match.kt / MatchSet.kt / League.kt   # Data models
        ├── MatchManager.kt          # Parses match data, notifies listeners
        ├── MatchDataService.kt      # REST client (OkHttp) for match data
        └── LiveScoreWebSocketManager.kt  # WebSocket client for live updates
```

**Key patterns:**
- Listener interfaces for async callbacks (`MessageListener`, `MatchManagerListener`, `MatchDataListener`, `WebSocketListener`)
- `IEchoClient` interface enables dependency injection for testing `LedMatrix`
- Global state via companion objects (`MainActivity.ledMatrix`, `LedMatrix.matrixAddress`)
- UDP port 4210 for matrix commands, port 4445 for discovery

## Testing Requirements

**Every code change must include corresponding tests.** This is a strict project rule.

- Use mocks/test doubles for network dependencies (see `TestEchoServer`, `TestLiveScoreWebSocketManager`)
- Unit tests: `app/src/test/` (JUnit 4 + Mockito)
- UI/integration tests: `app/src/androidTest/` (Espresso + AndroidJUnit4)
- Refactor for testability when needed: open classes, interfaces, constructor injection
- UI tests use `ScoreboardActivity.disableInformerForTests` to suppress timers

## Dependencies

- **Networking:** OkHttp 4.12.0, Java-WebSocket 1.5.7, built-in DatagramSocket (UDP)
- **Testing:** JUnit 4.13.2, Mockito 4.11.0 (core + inline + android), Espresso 3.5.1
- **Android:** AppCompat 1.7.0, ConstraintLayout 2.2.0, Material 1.12.0
