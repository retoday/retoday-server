package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Page

interface CustomPageRepository {
    fun upsertByUrl(page: Page): Page
}
