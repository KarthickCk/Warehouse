plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

group = "org.warehouse"
version = "0.1.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
}

application {
    mainClass = "org.warehouse.AppKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("failed", "passed", "skipped")
    }
}
