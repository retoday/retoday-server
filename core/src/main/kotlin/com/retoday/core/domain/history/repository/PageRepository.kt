package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Page
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PageRepository :
    JdbcRepository<Page, UUID>,
    CustomPageRepository {
    fun getByUrl(url: String): Page
}
