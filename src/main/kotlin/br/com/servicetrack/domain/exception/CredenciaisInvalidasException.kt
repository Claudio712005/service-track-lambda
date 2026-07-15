package br.com.servicetrack.domain.exception

class CredenciaisInvalidasException(
    message: String = "Credenciais inválidas"
) : RuntimeException(message)
