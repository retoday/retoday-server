plugins {
    alias(libs.plugins.jib)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.spring.batch)
    implementation(libs.spring.actuator)
    implementation(libs.prometheus)
    implementation(libs.prometheus.pushgateway)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.spring.ai.gemini)
    testImplementation(libs.jackson.kotlin)
    testImplementation(libs.bundles.test)

    testFixturesImplementation(testFixtures(project(":core")))
}

tasks {
    bootJar {
        enabled = true
    }

    jar {
        enabled = false
    }

    bootRun {
        systemProperty("user.timezone", "UTC")
    }
}

jib {
    from {
        image = "amazoncorretto:21-alpine"
    }

    container {
        jvmFlags = listOf("-Duser.timezone=UTC")
    }
}
