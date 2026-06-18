package com.retoday.api.domain.user.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.retoday.core.domain.user.dto.command.WithdrawCommand
import jakarta.validation.constraints.NotBlank

data class WithdrawRequest(
    @field:NotBlank
    @get:JsonProperty("oAuthToken")
    val oAuthToken: String
) {
    fun toCommand(): WithdrawCommand = WithdrawCommand(oAuthToken = oAuthToken)
}
