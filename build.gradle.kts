plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

group = "org.warehouse.app"
version = "0.1.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
    implementation(libs.slf4jApi)
    runtimeOnly(libs.slf4jSimple)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.konsist)
}

application {
    mainClass = "org.warehouse.measure.app.AppKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("failed", "passed", "skipped")
    }
}
