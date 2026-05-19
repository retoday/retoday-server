package com.retoday.api.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import java.time.LocalDate

data class GetMyRecapRequest(
    val date: LocalDate
) {
    fun toQuery(): GetMyRecapQuery = GetMyRecapQuery(date = date)
}
