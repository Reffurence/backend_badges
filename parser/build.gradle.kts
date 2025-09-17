plugins {
    java
    antlr
}

group = "net.foxboi.mlem"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.5")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}