package br.com.servicetrack.domain.vo

import java.util.UUID

@JvmInline
value class UsuarioId private constructor(
    val valor: String
) {

    companion object {
        fun gerar() = UsuarioId(UUID.randomUUID().toString())
        fun de(valor: String) = UsuarioId(valor)
    }
}