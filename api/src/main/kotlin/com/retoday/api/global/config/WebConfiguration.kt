package com.retoday.api.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(
    @Value($$"${frontend.uris}")
    private val frontendUris: Array<String>
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(*frontendUris)
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
