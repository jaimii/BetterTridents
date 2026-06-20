plugins {
    kotlin("jvm") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "8.3.10"
}

group = "project.kompass"
version = "1.4"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // Bundles the Kotlin standard library into the output jar
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    // Direct paperweight to automatically use the shadow jar as the reobfuscation source
    shadowJar {
        // archiveClassifier.set("") -> Removed to resolve the reobfJar duplicate output configuration error

        // Relocate Kotlin to prevent classpath conflicts with other plugins
        relocate("kotlin", "project.kompass.btk.libs.kotlin")
    }
}