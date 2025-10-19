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
}