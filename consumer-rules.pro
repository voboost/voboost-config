# Consumer ProGuard rules for voboost-config library
# These rules will be automatically applied to projects that consume this library

# Keep Hoplite classes that might be used by reflection
-keep class com.sksamuel.hoplite.** { *; }

# Keep all configuration model classes as they are used for YAML parsing
-keep class ru.voboost.config.models.** { *; }

# Keep public API
-keep public class ru.voboost.config.ConfigManager { *; }
-keep public interface ru.voboost.config.OnConfigChangeListener { *; }

# Keep data class constructors and fields for Hoplite
-keepclassmembers class ru.voboost.config.models.** {
    <init>(...);
    <fields>;
}