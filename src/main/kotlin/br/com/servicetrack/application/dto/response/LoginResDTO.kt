package br.com.servicetrack.application.dto.response

import br.com.servicetrack.domain.model.TokenAutenticacao
import kotlinx.serialization.Serializable

@Serializable
data class LoginResDTO(
    val token: String,
    val tipo: String,
    val expiraEmSegundos: Long
) {
    companion object {
        fun de(token: TokenAutenticacao) = LoginResDTO(
            token = token.token,
            tipo = token.tipo,
            expiraEmSegundos = token.expiraEmSegundos
        )
    }
}
