/**
 * CoreVelocity
 */
plugins {
    id("io.github.goooler.shadow")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.kryptonmc.org/releases")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com")
    maven("https://repo.william278.net/releases/")
    maven {
        url = uri("https://repo.minebench.de/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation(project(":core-common"))
    implementation("org.ocpsoft.prettytime:prettytime:5.0.9.Final")

    implementation("dev.dejvokep:boosted-yaml:1.3.4")
    implementation("de.themoep:minedown-adventure:1.7.3-SNAPSHOT")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.apache.commons:commons-pool2:2.12.0")

    implementation("io.github.revxrsal:lamp.common:4.0.0-beta.24")
    implementation("io.github.revxrsal:lamp.velocity:4.0.0-beta.24")
    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-beta.24")

    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.neznamy:tab-api:4.0.0")
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnly("net.william278:papiproxybridge:1.5")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")

        // Relocations
        relocate("dev.dejvokep.boostedyaml", "fr.iban.velocitycore.libs.boostedyaml")
        relocate("de.themoep.minedown", "fr.iban.velocitycore.libs.minedown")
        relocate("com.mysql.cj", "fr.iban.velocitycore.libs.mysql")
        relocate("org.apache.commons.pool2", "fr.iban.velocitycore.libs.commons.pool2")

        // From common
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
                description.set("Velocity implementation of ServerCore")
            }
        }
    }
}

tasks.named("publishMavenPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar)
}