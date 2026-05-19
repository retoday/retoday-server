package com.retoday.core.domain.recap.dto.response

data class GenerateTopicsResponse(
    val topics: List<Topic>
) {
    data class Topic(
        val keyword: String,
        val title: String,
        val content: String
    )
}
