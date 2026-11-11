package br.com.servicetrack.infrastructure.adapter.usuarios

import kotlinx.serialization.Serializable

@Serializable
data class VerificacaoResDTO(
    val id: String,
    val documento: String,
    val nome: String,
    val tipoDeUsuario: String,
    val roles: Set<String>,
)
