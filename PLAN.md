# План реализации проекта voboost-config

## 1. Структура проекта (Библиотека)

Проект `voboost-config`, находящийся в этой директории, является **модулем Android-библиотеки (`com.android.library`)**. Вся его структура находится в текущей рабочей директории.

```
/Users/vitaly/voboost/voboost-config/
├── build.gradle.kts
├── src/
│   └── main/
│       ├── java/ru/voboost/config/
│       └── AndroidManifest.xml
└── ... (файлы документации)
```

**Примечание:** Демонстрационное приложение (`voboost-config-demo`) является **отдельным, независимым проектом** и должно находиться в соседней директории. Его создание и настройка выходят за рамки данной рабочей области. Оно будет импортировать библиотеку `voboost-config` для тестирования.

## 2. Этапы реализации библиотеки

### Этап 1: Настройка проекта библиотеки

1.  Создать необходимую структуру файлов и директорий для Android-библиотеки в текущей директории.
2.  Настроить `build.gradle.kts`:
    *   Применить плагин `com.android.library`.
    *   Установить `minSdk = 28`.
    *   Добавить зависимости Hoplite: `com.sksamuel.hoplite:hoplite-core` и `com.sksamuel.hoplite:hoplite-yaml`.

### Этап 2: Реализация API библиотеки

1.  **Создать модели данных** в пакете `ru.voboost.config.models`.
2.  **Создать интерфейс слушателя** `OnConfigChangeListener` в пакете `ru.voboost.config.listeners`.
3.  **Реализовать фасад `ConfigManager`** с публичными методами.
4.  **Реализовать внутреннюю логику** отслеживания файлов и сравнения объектов.

## 3. Концепция интеграции с демо-приложением

Для локального тестирования, в `settings.gradle.kts` демо-приложения `voboost-config-demo` можно будет подключить библиотеку, используя `includeBuild`:

```kotlin
// Пример для settings.gradle.kts в проекте voboost-config-demo
pluginManagement {
    repositories {
        // ...
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // ...
    }
}
rootProject.name = "VoboostConfigDemo"
include(":app")

// Подключение локальной библиотеки
includeBuild("../voboost-config")