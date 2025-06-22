# Project Brief: voboost-config

## 1. Project Overview

`voboost-config` is a **standalone, reusable Android library (SDK)** written in Kotlin for API 28+, designed for application configuration management. The library provides a unified way to read, write, and track changes in YAML configuration files.

## 2. Main Goal

The primary goal of the project is to encapsulate all logic for working with configuration files, providing host applications (such as the separate `voboost-config-demo` application or others) with a simple, reliable, and type-safe API. This will allow for centralized management of settings and dynamic changes to application behavior without requiring a rebuild or restart.

## 3. Key Library Requirements

*   **Read Configuration**: Parse a YAML file into a strictly-typed Kotlin object (`data class`).
*   **Write Configuration**: Save the configuration object back to a YAML file.
*   **Change Tracking**: Track changes to the configuration file in real-time.
*   **Change Differentiation (Diff)**: Provide an object containing only the modified fields.
*   **Target Platform**: Android 9 (API 28) and higher.
*   **Primary Language**: Kotlin.
*   **Dependencies**: Use the `hoplite` library for handling YAML.
*   **Testability**: The library must be easily testable. Testing will be performed using a separate, independent demo application, `voboost-config-demo`.

## 4. Configuration Structure

The configuration has 5 root sections:
- `store`
- `applications`
- `interface`
- `vehicle`
- `settings`