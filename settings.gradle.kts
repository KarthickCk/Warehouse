// https://docs.gradle.org/current/userguide/settings_file_basics.html

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Automatically downloads JDKs required by the build (see gradle/gradle-daemon-jvm.properties and jvmToolchain).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "Warehouse"
