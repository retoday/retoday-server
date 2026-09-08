package com.retoday.api.domain.user.dto.request

import com.retoday.api.global.validation.Domain
import com.retoday.core.domain.user.dto.command.AddMyExcludedDomainCommand

data class AddMyExcludedDomainRequest(
    @field:Domain
    val domain: String
) {
    fun toCommand(): AddMyExcludedDomainCommand = AddMyExcludedDomainCommand(domain = domain)
}
