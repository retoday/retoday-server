package com.retoday.core.common

import com.retoday.core.global.config.JdbcConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers as EnableTestContainers

@DataJdbcTest
@EnableTestContainers
@AutoConfigureJooq
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [JdbcConfiguration::class])
abstract class RepositoryTest : DescribeSpec() {
    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        private val mysql =
            MySQLContainer("mysql:8.0")
                .withUrlParam("connectionTimeZone", "UTC")
                .withUrlParam("useAffectedRows", "true")
    }
}
