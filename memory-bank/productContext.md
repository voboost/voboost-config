# Product Context: voboost-config

## 1. The Problem

In modern, complex Android applications, especially in the automotive sector (in-vehicle infotainment), there is a large number of settings that can change depending on the user, car model, region, or even time of day.

Current approaches to configuration management often lead to the following issues:
*   **Hard-coded values**: Settings are embedded in the code, requiring recompilation and application updates for any changes.
*   **Difficulty of dynamic updates**: Changing application behavior on-the-fly without a restart requires complex and error-prone logic.
*   **Lack of centralization**: Configuration is scattered across different parts of the application (SharedPreferences, SQLite, constants), making management and debugging difficult.
*   **Lack of type safety**: Working with configuration via `SharedPreferences` or `Bundle` is not type-safe and can lead to runtime errors.

## 2. The Solution

The `voboost-config` library aims to solve these problems by providing a centralized, reliable, and dynamic configuration management mechanism.

*   **Centralization**: All configuration is stored in a single YAML file, which serves as the "single source of truth."
*   **Type Safety**: Through the use of Kotlin `data class` and the `hoplite` library, access to the configuration becomes fully type-safe, eliminating an entire class of errors.
*   **Dynamism**: Using a change tracking mechanism (`FileObserver`), applications can instantly react to external changes in the configuration (e.g., made through a diagnostic tool or admin panel), updating their UI and behavior without a restart.
*   **Transparency of Changes**: The "diff" function allows for precise understanding of *what exactly* has changed, enabling reactions to specific modifications rather than just reloading all settings.

## 3. Developer Experience

The target audience for the library is developers of two (or more) applications. `voboost-config` must provide them with the simplest and most intuitive experience possible:
1.  **Simple Integration**: Minimal steps to connect and get started.
2.  **Clear API**: Clean and predictable methods in `ConfigManager`.
3.  **Reliability**: The library must handle errors correctly (e.g., invalid YAML or missing file) and not cause the host application to crash.
4.  **Good Documentation and Example**: The presence of `voboost-config-demo` as a reference example of usage.