package com.retoday.api.domain.user.dto.request

import com.retoday.api.global.validation.Domain
import com.retoday.core.domain.user.dto.command.AddMyExcludedDomainCommand
import jakarta.validation.constraints.NotBlank

data class AddMyExcludedDomainRequest(
    @field:NotBlank
    @field:Domain
    val domain: String
) {
    fun toCommand(): AddMyExcludedDomainCommand = AddMyExcludedDomainCommand(domain = domain)
}
