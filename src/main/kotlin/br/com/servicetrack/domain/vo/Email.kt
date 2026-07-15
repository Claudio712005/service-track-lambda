package br.com.servicetrack.domain.vo

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Email(val valor: String) {

    init {
        if (!REGEX.matches(valor)) {
            throw DomainException("Email inválido")
        }
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}