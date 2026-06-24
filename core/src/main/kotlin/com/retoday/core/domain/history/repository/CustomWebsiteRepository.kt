package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Website

interface CustomWebsiteRepository {
    /**
     * 도메인 기준으로 웹사이트를 upsert하고, 새로 등록되었는지 여부를 반환한다.
     *
     * @return 신규 INSERT면 true, 이미 존재하던 웹사이트면 false
     */
    fun upsertByDomain(website: Website): Boolean
}
