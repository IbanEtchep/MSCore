plugins {
    id("io.github.goooler.shadow")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://jitpack.io")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    implementation(project(":core-common"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("net.essentialsx:EssentialsX:2.20.0")
    compileOnly("com.github.plan-player-analytics:Plan:5.5.2150")

    implementation("io.github.revxrsal:lamp.common:4.0.0-beta.24")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-beta.24")
    implementation("com.github.technicallycoded:FoliaLib:main-SNAPSHOT")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")

        relocate("com.tcoded.folialib", "fr.iban.servercore.libs.folialib")
        relocate("com.zaxxer.hikari", "fr.iban.servercore.libs.hikari")
        relocate("redis.clients.jedis", "fr.iban.servercore.libs.jedis")
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
                description.set("Paper implementation of ServerCore")
            }
        }
    }
}

tasks.named("publishMavenPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar)
}