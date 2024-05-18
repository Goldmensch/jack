plugins {
    id("java")
    id("application")
}

group = "io.github.goldmensch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.tomlj", "tomlj", "1.1.1");
    implementation("org.apache.maven", "maven-model", "3.9.6")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "io.github.goldmensch.Jack"
    executableDir = "testDir"
}