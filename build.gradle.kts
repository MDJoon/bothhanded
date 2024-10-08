import org.gradle.configurationcache.extensions.capitalized

plugins {
    id("io.papermc.paperweight.userdev") version "1.7.3"
    kotlin("jvm") version "1.9.20"
}

group = "com.github.mdjoon"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
            expand(project.extra.properties)
        }
    }

    fun registerJar(
        classifier: String,
        source: Any
    ) = register<Copy>("${classifier}Jar") {
        from(source)

        val prefix = project.name
        val plugins = rootProject.file(".server/plugins-$classifier")
        val update = File(plugins, "update")
        val regex = Regex("($prefix).*(.jar)")

        from(source)
        into(if (plugins.listFiles { _, it -> it.matches(regex) }?.isNotEmpty() == true) update else plugins)

        doLast {
            update.mkdirs()
            File(update, "RELOAD").delete()
        }
    }
    registerJar("paper", reobfJar)
}