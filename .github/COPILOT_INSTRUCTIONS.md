# GitHub Copilot Instructions for Scoreboard Project

## General Guidelines
- Every code change (feature, bugfix, or refactor) **must** be accompanied by a corresponding unit test or integration/UI test that verifies the new or changed behavior.
- If you refactor code for testability (e.g., making classes open, introducing interfaces, or injecting dependencies), ensure that new or updated tests demonstrate the improved test coverage.
- Use mocks or test doubles for network and hardware dependencies (e.g., WebSocket, EchoServer, LED matrix) in your tests.
- All tests should be runnable via `./gradlew test` (unit) or `./gradlew connectedAndroidTest` (integration/UI).
- Review the test suite after every change to confirm coverage and correctness.

## Recent Learnings and Best Practices
- Refactor classes to be open (not final) or use interfaces to allow mocking and subclassing in tests.
- Inject dependencies (such as network clients or hardware interfaces) to make code more testable.
- Always update or add tests when making code easier to test.
- Ensure that UI and integration tests use proper test doubles and simulate real-world scenarios.
- Document any changes to testability or testing strategy in the README and this file.

## Example Commit Message
```
Refactor LedMatrix for testability, inject IEchoClient, and add/expand unit tests
```

---

**Copilot and contributors:**
- Never submit a code change without a corresponding test.
- If you change the architecture for testability, prove it with new or improved tests.
- Keep this file and the README up to date with any new testing or contribution requirements.

