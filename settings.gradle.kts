pluginManagement {
    val version_kotlin: String by settings

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

        kotlin("jvm") version version_kotlin
        kotlin("plugin.serialization") version version_kotlin
    }
}

val project_name: String by settings

rootProject.name = project_name

include(":parser")
