plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    api(project(":shared:domain"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
