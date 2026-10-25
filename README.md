# Quos Agent

Quos Agent is a privacy-focused Android personal automation assistant. It stores local tasks and notes in Room, schedules network-dependent work in WorkManager, and keeps user approvals explicit before risky actions are executed.

## Minimum requirements
- Android Studio Ladybug or newer with Android Gradle Plugin 8.5.2
- JDK 17
- Android SDK 34
- minimum API level 26

## Build and run
1. Open Android Studio.
2. Import the project root.
3. Let Gradle sync.
4. Create a device or emulator running Android 8.0+.
5. Run the app configuration `app`.

## Command examples supported by the offline deterministic parser
- Save a note about my new app idea.
- Show pending tasks.
- Prepare an email to my project team.
- Queue this task until the internet returns.
- Cancel task 123.

## OAuth / Gmail setup
This app includes a placeholder provider abstraction and OAuth config scaffolding only. No secrets are committed. To connect a real Gmail account:

1. Create a Google Cloud project.
2. Enable the Gmail API.
3. Add OAuth 2.0 client credentials for Android or a web client as appropriate.
4. Add `CLIENT_ID`, `CLIENT_SECRET`, and redirect URI values in a local secure config file or your secure backend.
5. Keep tokens in Android Keystore or a secure backend, never as hardcoded strings in the client.

## Privacy and data boundaries
- Stored locally on the device: notes, tasks, event logs, user-approved drafts, and scheduling metadata.
- Leave the device only when the user approves a cloud action or provider request (such as Gmail OAuth or a backend API call).
- No passwords or secrets are stored in source code or logs.

## Known limitations
- This is an offline-first starter app that implements the core architecture and functional UI.
- External provider integrations (Gmail OAuth, remote AI planning) require additional configuration outside the app.
- The app does not send any real email in automated tests.

## Testing
Run:

./gradlew testDebugUnitTest

or open Android Studio's test runner to run the unit tests and instrumentation tests for the app module.
