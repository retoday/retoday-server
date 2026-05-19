plugins {
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
}

dependencies {
    implementation(libs.spring.web)
    implementation(libs.spring.log4j2)
    implementation(libs.spring.data.jdbc)
    implementation(libs.spring.data.redis)
    implementation(libs.spring.ai.gemini)
    implementation(libs.kotlin.logging)
    implementation(libs.jackson.kotlin)
    implementation(libs.uuid)
    implementation(libs.bundles.jwt)
    implementation(libs.bundles.jooq)
    jooqCodegen(libs.jooq.meta)

    runtimeOnly(libs.mysql.driver)
    runtimeOnly(libs.bundles.flyway)

    testImplementation(libs.spring.test)
    testImplementation(libs.bundles.test)
    testFixturesImplementation(libs.spring.data.jdbc)
    testFixturesImplementation(libs.bundles.test)
    testFixturesImplementation(libs.bundles.testcontainers)
}

tasks {
    bootJar {
        enabled = false
    }

    compileKotlin {
        dependsOn(jooqCodegen)
    }

    runKtlintCheckOverMainSourceSet {
        dependsOn(jooqCodegen)
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated")
        }
    }
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"

                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/*.sql"
                    }

                    property {
                        key = "sort"
                        value = "flyway"
                    }

                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }

                forcedTypes {
                    forcedType {
                        userType = "java.util.UUID"
                        converter = "com.retoday.core.global.converter.UuidConverter"
                        includeTypes = "BINARY\\(16\\)"
                        includeExpression = "(?i).*\\.(id|.*_id)$"
                    }

                    forcedType {
                        userType = "java.time.Instant"
                        converter = "com.retoday.core.global.converter.InstantConverter"
                        includeTypes = "TIMESTAMP\\(6\\)"
                        includeExpression = ".*\\..*_at"
                    }
                }
            }

            generate {
                isPojos = false
                isKotlinNotNullRecordAttributes = true
            }

            target {
                packageName = "com.retoday.core.global.jooq"
                directory = "build/generated"
            }
        }
    }
}
