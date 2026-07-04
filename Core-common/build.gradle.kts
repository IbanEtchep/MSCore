/**
 * CoreCommon
 */
plugins {
    id("io.github.goooler.shadow")
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.3")
    compileOnly("com.google.code.gson:gson:2.10")
    implementation("org.jdbi:jdbi3-core:3.49.6")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("net.kyori:adventure-api:4.17.0")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.17.0")
}

tasks.test {
    useJUnitPlatform()
}