package com.retoday.core.domain.recap.dto.response

data class GenerateRecapResponse(
    val title: String,
    val summary: String,
    val sections: List<Section>
) {
    data class Section(
        val title: String,
        val content: String
    )
}
