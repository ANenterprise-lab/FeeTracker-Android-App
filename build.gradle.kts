// In your PROJECT-level build.gradle.kts
plugins {
    // Make the application plugin available with a specific version
    id("com.android.application") version "8.12.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}