plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"

    idea
    eclipse

    application
}

group = "net.foxboi"
version = "1.0-beta.2"

application {
    mainClass = "net.foxboi.badger.MainKt"
}

idea {
    module {
        isDownloadSources = true
    }
}

eclipse {
    classpath {
        isDownloadSources = true
    }
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}


val osName: String = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val targetArch = when (val osArch: String = System.getProperty("os.arch")) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "$targetOs-$targetArch"

dependencies {
    // Parser
    implementation("org.antlr:antlr4:4.5")
    implementation(project(":parser"))

    // Skia
    implementation("org.jetbrains.skiko:skiko:0.8.9")
    runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-$target:0.8.9")

    // PDF
    implementation("com.github.librepdf:openpdf:2.0.3")

    // Ktor
    implementation("io.ktor:ktor-server-core:3.3.0")
    implementation("io.ktor:ktor-server-cio:3.3.0")
    implementation("io.ktor:ktor-client-core:3.3.0")
    implementation("io.ktor:ktor-client-cio:3.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.21")

    // Serialisation
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.25.1")
    implementation("org.apache.logging.log4j:log4j-api:2.25.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.1")

    // Kaml
    implementation("com.charleskorn.kaml:kaml:0.96.0")

    // MinIO
    implementation("io.minio:minio:8.5.17") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    // MinIO uses a vulnerable version of commons-compress,
    // they fixed it recently by updating to commons-compress
    // 1.28.0 but this has not yet been released. Let's manually
    // replace it.
    implementation("org.apache.commons:commons-compress:1.28.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.run.configure {
    workingDir = file("$projectDir")
    environment("CONFIG_PATH" to "./example_config.yml")
}