package br.com.servicetrack.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ErroResDTO(
    val mensagem: String,
    val requestId: String?
)
