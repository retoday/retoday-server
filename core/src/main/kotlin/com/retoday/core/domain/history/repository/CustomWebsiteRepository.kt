package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Website

interface CustomWebsiteRepository {
    fun upsertByDomain(website: Website): Website
}
