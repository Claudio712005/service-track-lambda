package br.com.servicetrack.infrastructure.adapter.usuarios

data class VerificacaoReqDTO(
    val documento: String,
    val senha: String,
)
