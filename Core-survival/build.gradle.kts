/**
 * CoreSurvival
 */

plugins {
    id("io.github.goooler.shadow")
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://jitpack.io")
    maven("https://maven.playpro.com")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly(project(":core-common"))
    compileOnly(project(":core-paper"))
    compileOnly("org.ocpsoft.prettytime:prettytime:5.0.9.Final")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.essentialsx:EssentialsX:2.20.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly(files("${projectDir}/lib/BetterRTP-3.6.13.jar"))

    compileOnly("io.github.revxrsal:lamp.common:4.0.0-beta.24")
    compileOnly("io.github.revxrsal:lamp.bukkit:4.0.0-beta.24")

    implementation("com.github.technicallycoded:FoliaLib:main-SNAPSHOT")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("com.tcoded.folialib", "fr.iban.servercore.libs.folialib")
    }

    compileJava {
        dependsOn(":core-paper:shadowJar")
    }

    register<Copy>("copyJar") {
        doFirst {
            mkdir("../libs/")
        }
        from(named("shadowJar"))
        into("../libs/")
    }

    build {
        dependsOn(shadowJar)
        finalizedBy("copyJar")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            artifact(tasks.named("shadowJar"))

            pom {
                name.set(project.name)
                description.set("Survival implementation of ServerCore")
            }
        }
    }
}

tasks.named("publishMavenPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar)
}