package com.retoday.batch.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(basePackages = ["com.retoday.core"])
@ComponentScan(basePackages = ["com.retoday.batch", "com.retoday.core"])
class ScanConfiguration
