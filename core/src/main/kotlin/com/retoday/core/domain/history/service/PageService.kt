package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.command.UpsertPageCommand
import com.retoday.core.domain.history.entity.Page
import com.retoday.core.domain.history.repository.PageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PageService(
    private val pageRepository: PageRepository
) {
    @Transactional
    fun upsertPage(command: UpsertPageCommand): Page =
        with(command) {
            pageRepository.upsertByUrl(
                Page(
                    websiteId = websiteId,
                    url = url,
                    title = title,
                    description = description
                )
            )
        }
}
