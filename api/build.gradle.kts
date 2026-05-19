import com.epages.restdocs.apispec.gradle.OpenApi3Task

plugins {
    alias(libs.plugins.restdocs.api.spec)
    alias(libs.plugins.jib)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.spring.web)
    implementation(libs.spring.security)
    implementation(libs.spring.validation)
    implementation(libs.spring.actuator)
    implementation(libs.kotlin.logging)
    implementation(libs.prometheus)
    implementation(libs.bundles.jwt)
    implementation(libs.bundles.jackson)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.spring.restdocs)

    testFixturesImplementation(testFixtures(project(":core")))
    testFixturesImplementation(libs.bundles.test)
    testFixturesImplementation(libs.bundles.spring.restdocs)
    testFixturesImplementation(libs.spring.security)
}

tasks {
    bootJar {
        enabled = true
    }

    jar {
        enabled = false
    }

    test {
        finalizedBy(withType<OpenApi3Task>())
    }

    bootRun {
        systemProperty("user.timezone", "UTC")
    }

    withType<OpenApi3Task> {
        doFirst {
            file(openapi3.outputDirectory).mkdirs()
        }
    }
}

openapi3 {
    title = "Retoday API"
    description = "Retoday API Documentation"
    version = project.version.toString()
    format = "yaml"
    outputFileNamePrefix = "api"
    outputDirectory = "src/main/resources/static/docs"
    setServer("/api/v1")
}

jib {
    from {
        image = "amazoncorretto:21-alpine"
    }

    container {
        jvmFlags = listOf("-Duser.timezone=UTC")
    }
}
