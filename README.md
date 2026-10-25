# Quos Personal AI Agent

Quos Personal AI Agent is a privacy-first Android personal productivity app that helps users manage tasks, notes, email drafts, and scheduled local actions through a deterministic command flow and safe approval model. The app is designed to keep all sensitive work local by default and only act on user-approved requests.

## What the app does
- Stores notes and task metadata locally using Room.
- Lets users issue natural-language commands such as saving a note, viewing tasks, or queueing work.
- Uses a safe local planner and validation layer before actions are executed.
- Supports explicit email approval before any send is triggered.
- Queues network-dependent work for later execution using WorkManager.
- Keeps user approvals explicit for critical actions and avoids unsafe automation.

## Project status
This repository contains the Android app skeleton and feature foundation for a local-first personal AI agent. The project is structured for future expansion into remote AI integrations, email automation, and more advanced productivity workflows.

## Minimum requirements
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34
- Min SDK 26
- Android Gradle Plugin 8.5.2

## How to build
1. Open the project in Android Studio.
2. Let Gradle sync.
3. Select the app configuration and run on an emulator or connected device.
4. Run the unit tests with:
   ./gradlew testDebugUnitTest

## Privacy and safety model
- Notes and tasks are retained on-device by default.
- User approval is required before important actions are executed.
- Network-dependent jobs are stored and retried safely when connectivity is available.
- No secrets are committed to the repository.

## Repository description
Quos Personal AI Agent is a local-first personal assistant app for Android that focuses on note-taking, task scheduling, safe automation, and explicit email approvals.
