import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// https://github.com/gradle/gradle/issues/22797
// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotest)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.example"
version = 0.1

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.arrow.fx)
    implementation(libs.suspendapp)
    implementation(libs.suspendapp.ktor)
    implementation(libs.bundles.ktor.server)
    implementation(libs.kafka.streams)
    implementation(libs.logback)

    testImplementation(libs.bundles.kotest)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }

}
