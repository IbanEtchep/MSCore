plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
}


allprojects {
    apply(plugin = "java")

    group = "com.github.IbanEtchep.MSCore"
    version = "1.1.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
