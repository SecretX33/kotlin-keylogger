import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.github.secretx33"
version = "0.1"

val javaVersion = 17

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.1stleg:jnativehook:2.1.0")
}

tasks.test {
    useJUnitPlatform()
}

// Disables the normal jar task
tasks.jar { enabled = false }

// And enables shadowJar task
artifacts.archives(tasks.shadowJar)

tasks.shadowJar {
    archiveFileName.set("keylogger.jar")
    val dependencyPackage = "${rootProject.group}.dependencies.${rootProject.name.toLowerCase()}"
    relocate("kotlin", "${dependencyPackage}.kotlin")
    relocate("kotlinx", "${dependencyPackage}.kotlinx")
    relocate("org.intellij", "${dependencyPackage}.intellij")
    relocate("org.jetbrains", "${dependencyPackage}.jetbrains")
    exclude("ScopeJVMKt.class")
    exclude("DebugProbesKt.bin")
    exclude("META-INF/**")
}

tasks.withType<JavaCompile> {
    options.apply {
        release.set(javaVersion)
        encoding = "UTF-8"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = javaVersion.toString()
    }
}

application {
    mainClass.set("com.github.secretx33.keylogger.MainKt")
}