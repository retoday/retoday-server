package com.retoday.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = ["com.retoday.core", "com.retoday.batch"])
@SpringBootApplication(scanBasePackages = ["com.retoday.batch", "com.retoday.core"])
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}
