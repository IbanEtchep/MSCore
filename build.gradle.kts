plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

allprojects {
    apply(plugin = "java")

    group = "com.github.IbanEtchep.MSCore"
    version = "1.1.2"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.tcoded.com/releases")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
}

subprojects {
    tasks.processResources {
        filesMatching("plugin.yml") {
            expand(
                "project_version" to project.version
            )
        }
    }
}
