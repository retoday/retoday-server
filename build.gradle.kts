import extension.apply
import extension.exclude
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.java.test.fixtures)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.lint) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "com.retoday"
    version = "0.0.1"

    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    apply(rootProject.libs.plugins.java.library)
    apply(rootProject.libs.plugins.java.test.fixtures)
    apply(rootProject.libs.plugins.kotlin.jvm)
    apply(rootProject.libs.plugins.kotlin.spring)
    apply(rootProject.libs.plugins.kotlin.lint)
    apply(rootProject.libs.plugins.spring.boot)
    apply(rootProject.libs.plugins.spring.dependency.management)

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs =
                listOf(
                    "-Xjsr305=strict",
                    "-Xannotation-default-target=param-property"
                )
            jvmTarget = JvmTarget.JVM_21
        }
    }

    configurations {
        all {
            exclude(rootProject.libs.spring.logging)
        }
    }

    tasks {
        test {
            useJUnitPlatform()
            systemProperty("user.timezone", "UTC")
            classpath += files(sourceSets.main.map { it.output })
        }
    }
}
