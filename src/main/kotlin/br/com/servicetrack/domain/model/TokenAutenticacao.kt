package br.com.servicetrack.domain.model

data class TokenAutenticacao(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEmSegundos: Long
)
