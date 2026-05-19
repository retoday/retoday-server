package com.retoday.core.global.config

import com.retoday.core.global.converter.BytesToUuidConverter
import com.retoday.core.global.converter.UuidToBytesConverter
import com.retoday.core.global.extension.createUuid
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback

@Configuration
@EnableJdbcRepositories(basePackages = ["com.retoday.core.domain"])
class JdbcConfiguration {
    @Bean
    fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                UuidToBytesConverter,
                BytesToUuidConverter
            )
        )

    @Bean
    fun beforeConvertCallback(mappingContext: RelationalMappingContext): BeforeConvertCallback<Any> =
        BeforeConvertCallback {
            val persistentEntity = mappingContext.getRequiredPersistentEntity(it::class.java)
            val idProperty = persistentEntity.requiredIdProperty
            val accessor = persistentEntity.getPropertyAccessor(it)

            if (accessor.getProperty(idProperty) == null) {
                accessor.setProperty(idProperty, createUuid())
            }

            accessor.bean
        }
}
