plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.springframework.security:spring-security-crypto:6.4.3")
}

tasks.test {
    useJUnitPlatform()
}