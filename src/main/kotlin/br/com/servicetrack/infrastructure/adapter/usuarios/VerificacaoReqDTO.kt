package br.com.servicetrack.infrastructure.adapter.usuarios

import kotlinx.serialization.Serializable

@Serializable
data class VerificacaoReqDTO(
    val documento: String,
    val senha: String,
)
