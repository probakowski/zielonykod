plugins {
    id("java")
    id("application")
    id("com.github.spotbugs") version "5.0.14"
    id("me.champeau.jmh") version "0.7.1"
}

group = "pl.robakowski"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    minHeapSize = "2048m"
    maxHeapSize = "2048m"
    useJUnitPlatform()
}

val jmhInclude = System.getenv("jmhInclude")
if (jmhInclude != null) {
    jmh {
        includes.add(jmhInclude)
    }
}

val integration: SourceSet by sourceSets.creating

configurations[integration.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integration.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

with(integration) {
    java.srcDir("src/integration/java")
    compileClasspath += sourceSets.main.get().output
}

val e2eTask = tasks.register<Test>("e2e") {
    description = "Runs E2E tests."
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = integration.output.classesDirs
    classpath =
        configurations[integration.runtimeClasspathConfigurationName] + integration.output + sourceSets.main.get().output

    shouldRunAfter(tasks.test)

    minHeapSize = "8G"
    maxHeapSize = "8G"
    jvmArgs("-server")
}

spotbugs {
    excludeFilter.set(file("spotbugs.exclude.xml"))
}

application {
    mainClass.set("pl.robakowski.Launcher")
    applicationDefaultJvmArgs = listOf("-Xmx3G", "-Xms3G", "-server")
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

    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")
}
