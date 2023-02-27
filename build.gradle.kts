@file:Suppress("SpellCheckingInspection")

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`

}

group = "com.undermark5"
version = "0.1.0-alpha.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-11")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    api("com.expediagroup:graphql-kotlin-server:7.0.0-alpha.3")
    api("io.insert-koin:koin-core:3.3.2")
    implementation("io.insert-koin:koin-ktor:3.3.0")
    implementation("io.insert-koin:koin-annotations:1.1.0")
    implementation("com.apollographql.federation:federation-graphql-java-support:2.3.0")
    api("io.ktor:ktor-server-core:2.2.2")
    api("io.ktor:ktor-server-cio:2.2.2")
    implementation("io.ktor:ktor-server-websockets:2.2.2")
    implementation("io.ktor:ktor-server-cors:2.2.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.2")
    implementation("io.ktor:ktor-server-html-builder:2.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    ksp("io.insert-koin:koin-ksp-compiler:1.1.0")
    testImplementation(kotlin("test"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Undermark5-Software/graphql-kotlin-ktor-server")
            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

//kotlin {
//    jvmToolchain(8)
//}