package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Website
import com.retoday.core.global.extension.createUuid
import com.retoday.core.global.jooq.tables.Website.Companion.WEBSITE
import org.jooq.DSLContext
import org.jooq.impl.DSL.coalesce

class CustomWebsiteRepositoryImpl(
    private val dsl: DSLContext
) : CustomWebsiteRepository {
    override fun upsertByDomain(website: Website): Boolean {
        val websiteId = website.id ?: createUuid()

        dsl
            .insertInto(WEBSITE)
            .columns(
                WEBSITE.ID,
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL
            )
            .values(
                websiteId,
                website.domain,
                website.faviconUrl
            )
            .onDuplicateKeyUpdate()
            .set(WEBSITE.FAVICON_URL, coalesce(WEBSITE.FAVICON_URL, website.faviconUrl))
            .execute()

        // 영속화된 ID가 앞서 생성한 ID와 같으면 신규 INSERT
        val persistedId =
            dsl
                .select(WEBSITE.ID)
                .from(WEBSITE)
                .where(WEBSITE.DOMAIN.eq(website.domain))
                .fetchOne(WEBSITE.ID)

        return persistedId == websiteId
    }
}
