package com.retoday.api.domain.user.dto.request

import com.retoday.api.global.validation.Domain
import com.retoday.core.domain.user.dto.command.DeleteMyExcludedDomainCommand
import jakarta.validation.constraints.NotBlank

data class DeleteMyExcludedDomainRequest(
    @field:NotBlank
    @field:Domain
    val domain: String
) {
    fun toCommand(): DeleteMyExcludedDomainCommand = DeleteMyExcludedDomainCommand(domain = domain)
}
