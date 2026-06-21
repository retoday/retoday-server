package com.retoday.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class BatchApplication

fun main(args: Array<String>) {
    val context = runApplication<BatchApplication>(*args)
    val exitCode = SpringApplication.exit(context)

    exitProcess(exitCode)
}
