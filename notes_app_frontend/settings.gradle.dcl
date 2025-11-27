pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.experimental.android-ecosystem").version("0.1.43")
}

rootProject.name = "example-android-app"

include("app")
include("list")
include("utilities")

defaults {
    androidApplication {
        jdkVersion = 17
        compileSdk = 34
        minSdk = 30

        versionCode = 1
        versionName = "0.1"
        applicationId = "org.gradle.experimental.android.app"

        // Enable Jetpack Compose globally for androidApplication type
        compose {
            enable = true
            // Compose Compiler extension version compatible with Kotlin 2.0.21
            // androidx.compose.compiler:compiler 1.5.15 pairs with Kotlin 2.0.x
            kotlinCompilerExtensionVersion = "1.5.15"
        }

        // Kotlin version pin to ensure compatibility with Compose compiler extension
        kotlin {
            version = "2.0.21"
        }

        testing {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.2")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
        }
    }

    androidLibrary {
        jdkVersion = 17
        compileSdk = 34
        minSdk = 30

        // Enable Compose for libraries that might use compose UI in the future
        compose {
            enable = true
            kotlinCompilerExtensionVersion = "1.5.15"
        }

        kotlin {
            version = "2.0.21"
        }

        testing {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.2")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
        }
    }
}
