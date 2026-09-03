package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Page
import com.retoday.core.global.extension.createUuid
import com.retoday.core.global.extension.fetchSingleInto
import com.retoday.core.global.jooq.tables.Page.Companion.PAGE
import org.jooq.DSLContext

class CustomPageRepositoryImpl(
    private val dsl: DSLContext
) : CustomPageRepository {
    override fun upsertByUrl(page: Page): Page {
        dsl
            .insertInto(PAGE)
            .columns(
                PAGE.ID,
                PAGE.WEBSITE_ID,
                PAGE.URL,
                PAGE.TITLE,
                PAGE.DESCRIPTION
            )
            .values(
                page.id ?: createUuid(),
                page.websiteId,
                page.url,
                page.title,
                page.description
            )
            .onDuplicateKeyUpdate()
            .set(PAGE.TITLE, page.title)
            .set(PAGE.DESCRIPTION, page.description)
            .execute()

        return dsl
            .selectFrom(PAGE)
            .where(PAGE.URL.equal(page.url))
            .fetchSingleInto()
    }
}
