# Technical Context: voboost-config

## 1. Technology Stack

*   **Programming Language**: [Kotlin](https://kotlinlang.org/) - The official language for Android development, providing expressiveness, safety, and full Java interoperability.
*   **Platform**: Android.
*   **Minimum API Level**: API 28 (Android 9.0 Pie). This level was chosen as a balance between supporting relatively old devices and accessing modern platform features.
*   **IDE**: Android Studio.
*   **Build System**: Gradle.

## 2. Key Dependencies

*   **[Hoplite](https://github.com/sksamuel/hoplite)**: The core library for configuration management.
    *   **Modules**: `hoplite-core`, `hoplite-yaml`.
    *   **Reason for choice**: Hoplite provides a powerful and declarative way to map configuration files (including YAML) to strictly-typed Kotlin `data classes`. It supports nested classes, `enums`, and handles parsing errors, which is ideal for our task.

*   **[Android KTX](https://developer.android.com/kotlin/ktx)**: A set of Kotlin extensions that make Android development more concise and idiomatic. It will be used as needed.

## 3. Architecture and Patterns

*   **Project Structure**: The project is a **standalone Android library**. The demo application (`voboost-config-demo`) is a separate project that will consume the library.
*   **Facade Pattern**: The `ConfigManager` class acts as a facade, hiding the internal complexity of the library and providing a simple and clear public API.
*   **Observer Pattern**: `android.os.FileObserver` is used to track file changes, notifying subscribers (`OnConfigChangeListener`) of events.

## 4. Constraints and Features

*   **Filesystem Interaction**: The library will work with files in the application's private storage (`Context.getFilesDir()`) to avoid requesting permissions for shared storage. This is a more secure approach. The `voboost-config-demo` will demonstrate the pattern of copying a default config from `assets` to this storage.
*   **Thread Safety**: The initial version will not guarantee thread safety. It is assumed that calls to `ConfigManager` methods will be made from a single thread (e.g., the main UI thread). This may be improved in the future.