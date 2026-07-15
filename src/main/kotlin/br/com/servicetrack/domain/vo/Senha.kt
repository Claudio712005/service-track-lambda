package br.com.servicetrack.domain.vo

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Senha private constructor(
    val valor: String
) {
    companion object {
        private const val TAMANHO_MINIMO = 8

        fun de(valor: String): Senha {
            if (valor.length < TAMANHO_MINIMO) {
                throw DomainException("A senha deve ter no mínimo $TAMANHO_MINIMO caracteres.")
            }
            return Senha(valor)
        }
    }
}
