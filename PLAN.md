# Detailed Implementation Plan: `voboost-config` and `voboost-config-demo` (v4)

This plan outlines the necessary steps to build, test, and document the `voboost-config` Android library and its accompanying demo application, featuring a flattened data model.

## Phase 1: Project Scaffolding & Initial Setup

*   **Goal**: Establish the correct project structures and configurations.
*   **Outcome**: Two separate, correctly configured Android projects: one for the library and one for the demo app.
*   **Package Names**: `ru.voboost.config` and `ru.voboost.config.demo`.

## Phase 2: Library Core Implementation (`voboost-config`)

### 2.1: Define Flattened Configuration Data Model

*   **Task**: Create a single, flat Kotlin `data class` that maps to the nested YAML structure using `hoplite`'s `@ConfigAlias` annotation.
*   **File**: `ru.voboost.config.models.Config.kt`
*   **Structure**:
    ```kotlin
    package ru.voboost.config.models

    import com.sksamuel.hoplite.ConfigAlias

    // All possible configuration fields are defined here as a flat list.
    // The @ConfigAlias annotation maps the camelCase field name to the
    // dot-separated key in the YAML file.
    data class Config(
        @ConfigAlias("settings.language")
        val settingsLanguage: String? = null,

        @ConfigAlias("settings.theme")
        val settingsTheme: String? = null,

        @ConfigAlias("settings.interface-shift-x")
        val settingsInterfaceShiftX: Int? = null,

        @ConfigAlias("settings.interface-shift-y")
        val settingsInterfaceShiftY: Int? = null,

        @ConfigAlias("vehicle.drive-mode")
        val vehicleDriveMode: String? = null,

        // ... and so on for every other key in the YAML file.
        // For example:
        @ConfigAlias("applications.maps.provider")
        val applicationsMapsProvider: String? = null,

        @ConfigAlias("store.user.id")
        val storeUserId: String? = null
    )
    ```
*   **Rationale**: This powerful approach provides maximum flexibility. The YAML remains organized and readable, while the code interacts with a simple, flat, and fully type-safe object, with all properties at the top level. All properties are nullable to gracefully handle missing values in the config file.

### 2.2: Implement `ConfigManager` Facade

*   **File**: `ru.voboost.config.ConfigManager.kt`
*   **Public API Surface**:
    *   `fun loadConfig(context: Context, filePath: String): Result<Config>`
    *   `fun saveConfig(context: Context, filePath: String, config: Config)`
    *   `fun startWatching(context: Context, filePath: String, listener: OnConfigChangeListener)`
    *   `fun stopWatching()`

### 2.3: Implement `OnConfigChangeListener` Interface

*   **File**: `ru.voboost.config.OnConfigChangeListener.kt`
*   **Method**: `fun onConfigChanged(newConfig: Config, diff: Config)`

### 2.4: Implement Internal `ConfigFileObserver`
### 2.5: Implement "Diff" Logic

## Phase 3: Testing Strategy

*   **Goal**: Achieve high test coverage for all library features.
*   **Outcome**: A suite of unit tests validating the library's behavior.

## Phase 4: Demo Application (`voboost-config-demo`)

*   **Goal**: Create a functional console-based demonstration of the library's capabilities.
*   **Logic**:
    1.  On startup, copy a default `config.yaml` from `assets`.
    2.  Load the configuration using `ConfigManager`.
    3.  Log the resulting flat `Config` object to Logcat.
    4.  Start watching for file changes.
    5.  When a change occurs, log the new `Config` object and the `diff` object to Logcat.

## Phase 5: Documentation

*   **Goal**: Produce clear, comprehensive documentation.
*   **Outcome**: High-quality KDoc and `README.md` files.

## Mermaid Diagram of the Plan

```mermaid
gantt
    title Voboost-Config Implementation Plan
    dateFormat  YYYY-MM-DD
    axisFormat %m-%d

    section Phase 1: Scaffolding
    Create Library Project       :done, 2025-06-23, 1d
    Create Demo App Project      :2025-06-24, 1d
    Configure Lib Dependencies   :2025-06-25, 1d
    Configure Demo Dependencies  :2025-06-25, 1d

    section Phase 2: Library Implementation
    Define Data Models           :2025-06-26, 2d
    Implement ConfigManager      :2025-06-28, 3d
    Implement Change Listener    :2025-07-01, 1d
    Implement File Observer      :2025-07-02, 2d
    Implement Diff Logic         :2025-07-04, 2d

    section Phase 3: Testing
    Unit Tests for ConfigManager :2025-07-08, 3d
    Unit Tests for Diff Logic    :2025-07-11, 2d

    section Phase 4: Demo App
    Setup & Integrate Library    :2025-07-15, 3d
    Define Manual Test Scenarios :2025-07-18, 1d

    section Phase 5: Documentation
    KDoc for Public API          :2025-07-21, 2d
    README for Library           :2025-07-23, 2d
    README for Demo App          :2025-07-25, 1d