plugins {
    kotlin("jvm") version "2.3.0"
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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.mindrot:jbcrypt:0.4")

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