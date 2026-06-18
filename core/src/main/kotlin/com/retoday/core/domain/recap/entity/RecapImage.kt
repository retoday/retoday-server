package com.retoday.core.domain.recap.entity

import com.retoday.core.domain.history.entity.WebsiteCategory

enum class RecapImage {
    STUDY,
    SHOPPING,
    GAMING,
    CONTENT,
    COMMUNITY,
    NEWS,
    FINANCE,
    LIFE,
    BROWSING,
    DESIGN,
    AI,
    DEVELOPMENT,
    SCREEN_TIME_OVER_12H,
    SCREEN_TIME_UNDER_1H,
    CATEGORY_OVER_5,
    CATEGORY_ONLY_1,
    START_AFTER_9PM,
    START_BEFORE_9AM,
    RANDOM;

    companion object {
        fun from(category: WebsiteCategory): RecapImage? = entries.firstOrNull { it.name == category.name }
    }
}
