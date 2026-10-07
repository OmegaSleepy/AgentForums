plugins {
    application
    id("java")
}

group = "omegasleepy.github.io"
version = "0.0.1-alpha"

application {
    mainClass.set("io.github.omegasleepy.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

    implementation(libs.gson)
    implementation(libs.postgresql)

    implementation(libs.slf4j)
    implementation(libs.log4j)
    implementation(libs.log4j.core)
}

tasks.test {
    useJUnitPlatform()
}