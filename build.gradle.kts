import org.gradle.model.internal.core.ModelNodes.withType

plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    minHeapSize = "2048m"
    maxHeapSize = "2048m"
    jvmArgs = listOf("-XX:MaxPermSize=512m")
}

application {
    mainClass.set("pl.robakowski.Launcher")
    applicationDefaultJvmArgs = listOf("-Xmx3G", "-Xms3G", "-XX:MaxPermSize=512m")
}

dependencies {
    implementation("io.activej:activej-http:5.4.3")
    implementation("io.activej:activej-launchers-http:5.4.3")
    implementation("io.activej:activej-inject:5.4.3")

    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")

    implementation("com.dslplatform:dsl-json-java8:1.10.0")
    annotationProcessor("com.dslplatform:dsl-json-java8:1.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}