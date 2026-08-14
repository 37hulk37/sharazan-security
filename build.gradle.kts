plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
}

group = "com.sharazan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.sharazan:core:1.0-SNAPSHOT")
    implementation("com.sharazan:http:1.0-SNAPSHOT")
    implementation("com.sharazan:logging:1.0-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.20-RC")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.10.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

publishing {
    publications {
        create<MavenPublication>("publish") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}