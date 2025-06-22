# Full Orchestration Plan for `voboost-config`

This document provides a step-by-step checklist of tasks to be executed by an orchestration agent to implement the `voboost-config` library and demo application.

---

## Part 1: Library Project Setup (`voboost-config`)

*   [ ] **Task 1.1**: Create the main directory structure for the library.
    *   **Action**: Create directories `voboost-config/src/main/java/ru/voboost/config/models`, `voboost-config/src/main/java/ru/voboost/config/internal`, and `voboost-config/src/test/java/ru/voboost/config`.

*   [ ] **Task 1.2**: Create the `build.gradle.kts` file for the library module.
    *   **Action**: Create `voboost-config/build.gradle.kts` with the necessary plugins and dependencies for a Kotlin Android library, including `hoplite`, `coroutines`, and `junit`/`mockito`.

*   [ ] **Task 1.3**: Create the `AndroidManifest.xml` for the library.
    *   **Action**: Create a minimal `voboost-config/src/main/AndroidManifest.xml` with the correct package name.

---

## Part 2: Library Core Implementation

*   [ ] **Task 2.1**: Create the flattened `Config` data model.
    *   **Action**: Create `voboost-config/src/main/java/ru/voboost/config/models/Config.kt` with the flat `data class Config` structure using `@ConfigAlias` annotations for all fields.

*   [ ] **Task 2.2**: Create the `OnConfigChangeListener` interface.
    *   **Action**: Create `voboost-config/src/main/java/ru/voboost/config/OnConfigChangeListener.kt` with the `onConfigChanged` function signature.

*   [ ] **Task 2.3**: Create the `ConfigManager.kt` file with a skeleton object.
    *   **Action**: Create the file with the `ConfigManager` object and its four public methods (`loadConfig`, `saveConfig`, `startWatching`, `stopWatching`) containing `// TODO` comments.

*   [ ] **Task 2.4**: Implement the `loadConfig` method.
    *   **Action**: In `ConfigManager.kt`, implement the logic to read the file content from the given `filePath`, use `ConfigLoaderBuilder` from `hoplite` to parse the YAML string into a `Config` object, and wrap the result in `Result.success` or `Result.failure`.

*   [ ] **Task 2.5**: Implement the `saveConfig` method.
    *   **Action**: In `ConfigManager.kt`, implement the logic to convert the `Config` object back to a YAML string and write it to the specified `filePath`. (Note: This is a complex task and might require a custom dumper or relying on a library that supports this direction).

*   [ ] **Task 2.6**: Create the internal `ConfigFileObserver`.
    *   **Action**: Create `voboost-config/src/main/java/ru/voboost/config/internal/ConfigFileObserver.kt`. It should subclass `android.os.FileObserver`, take a callback lambda in its constructor, and call the lambda in the `onEvent` method when the event is `CLOSE_WRITE`.

*   [ ] **Task 2.7**: Implement `startWatching` and `stopWatching`.
    *   **Action**: In `ConfigManager.kt`, add a private `var` for the `ConfigFileObserver`. `startWatching` should instantiate the observer, store it, and start it. The observer's callback will trigger the diff logic and call the external listener. `stopWatching` should stop the observer and nullify the variable.

*   [ ] **Task 2.8**: Implement the private 'diff' logic.
    *   **Action**: In `ConfigManager.kt`, create a private function `createDiff(old: Config, new: Config): Config`. This function will compare each field of the two objects and construct a new `Config` object containing only the fields that have changed.

---
## Part 3: Library Testing

*   [ ] **Task 3.1**: Create a test resource directory and a sample `config.yaml`.
    *   **Action**: Create directory `voboost-config/src/test/resources`.
    *   **Action**: Create `voboost-config/src/test/resources/sample_config.yaml` with valid data.

*   [ ] **Task 3.2**: Create the test class for `ConfigManager`.
    *   **Action**: Create `voboost-config/src/test/java/ru/voboost/config/ConfigManagerTest.kt`.

*   [ ] **Task 3.3**: Add unit tests for `loadConfig`.
    *   **Action**: In `ConfigManagerTest.kt`, add tests for successfully loading the `sample_config.yaml`, loading a non-existent file, and loading a malformed YAML file. Use `Mockito` to mock the `Context`.

*   [ ] **Task 3.4**: Add unit tests for the `createDiff` logic.
    *   **Action**: In `ConfigManagerTest.kt`, add tests to verify the diffing logic: one test where there are no changes, one with a single change, and one with multiple changes in different sections.

---
## Part 4: Demo Application Setup (`voboost-config-demo`)

*   [ ] **Task 4.1**: Create the main directory structure for the demo app.
    *   **Action**: Create directories `voboost-config-demo/src/main/java/ru/voboost/config/demo`, and `voboost-config-demo/src/main/assets`.

*   [ ] **Task 4.2**: Create the `build.gradle.kts` for the demo app.
    *   **Action**: Create `voboost-config-demo/build.gradle.kts`. It should be configured as an application and include a dependency on the `voboost-config` library (e.g., `implementation(project(":voboost-config"))` if set up in a multi-module style for development).

*   [ ] **Task 4.3**: Create the `AndroidManifest.xml` for the demo app.
    *   **Action**: Create `voboost-config-demo/src/main/AndroidManifest.xml` for a basic application with one `MainActivity`.

*   [ ] **Task 4.4**: Create a default `config.yaml` in the demo's assets.
    *   **Action**: Create `voboost-config-demo/src/main/assets/config.yaml` with some default values.

*   [ ] **Task 4.5**: Create the `MainActivity.kt` skeleton.
    *   **Action**: Create `voboost-config-demo/src/main/java/ru/voboost/config/demo/MainActivity.kt`.

---
## Part 5: Demo Application Implementation

*   [ ] **Task 5.1**: Implement the logic to copy the config from assets to private storage on first run.
    *   **Action**: In `MainActivity.onCreate`, add code to check if the config file exists in private storage (`filesDir`). If not, copy it from `assets`.

*   [ ] **Task 5.2**: Implement the `ConfigManager` integration.
    *   **Action**: In `MainActivity.onCreate`, after ensuring the file exists, call `ConfigManager.loadConfig` and log the result. Then call `ConfigManager.startWatching`.

*   [ ] **Task 5.3**: Implement the `OnConfigChangeListener`.
    *   **Action**: Make `MainActivity` implement `OnConfigChangeListener`. In the `onConfigChanged` method, use `Log.i` to print the new full config and the diff object.

---
## Part 6: Documentation

*   [ ] **Task 6.1**: Create `README.md` for the library.
    *   **Action**: Create `voboost-config/README.md` and populate it with sections for Overview, Features, Installation (Gradle), and Usage examples.

*   [ ] **Task 6.2**: Create `README.md` for the demo app.
    *   **Action**: Create `voboost-config-demo/README.md` explaining its purpose and how to test with it using Logcat and the Device File Explorer.