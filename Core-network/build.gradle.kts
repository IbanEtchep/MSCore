/**
 * Cross-server services (messaging, presence, teleport) behind an interface,
 * with a single-server implementation and an MSCore-backed one.
 * Meant to be shaded and relocated by each consuming plugin.
 */
plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Only loaded at runtime when the MSCore plugin ("Core") is enabled.
    compileOnly(project(":core-paper"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    // core-paper targets Java 25; it is compileOnly here and never loaded on older runtimes.
    disableAutoTargetJvm()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Consumers build with Java 17 (Warps) or 21 (MSBoosts, MSGuilds).
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Network services abstraction for MS plugins, with a local and an MSCore implementation")
            }
        }
    }
}
