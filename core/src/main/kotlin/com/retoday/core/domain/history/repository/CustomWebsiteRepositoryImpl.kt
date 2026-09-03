package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Website
import com.retoday.core.global.extension.createUuid
import com.retoday.core.global.extension.fetchSingleInto
import com.retoday.core.global.jooq.tables.Website.Companion.WEBSITE
import org.jooq.DSLContext

class CustomWebsiteRepositoryImpl(
    private val dsl: DSLContext
) : CustomWebsiteRepository {
    override fun upsertByDomain(website: Website): Website {
        dsl
            .insertInto(WEBSITE)
            .columns(
                WEBSITE.ID,
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL
            )
            .values(
                website.id ?: createUuid(),
                website.domain,
                website.faviconUrl
            )
            .onDuplicateKeyUpdate()
            .set(WEBSITE.FAVICON_URL, website.faviconUrl)
            .execute()

        return dsl
            .selectFrom(WEBSITE)
            .where(WEBSITE.DOMAIN.equal(website.domain))
            .fetchSingleInto()
    }
}
