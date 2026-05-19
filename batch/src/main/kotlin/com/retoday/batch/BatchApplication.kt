package com.retoday.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.retoday.batch", "com.retoday.core"])
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}
