package br.com.servicetrack.domain.vo

import br.com.servicetrack.domain.exception.DomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SenhaTest {

    @Test
    fun `aceita senha com o tamanho minimo`() {
        assertEquals("12345678", Senha.de("12345678").valor)
    }

    @Test
    fun `rejeita senha abaixo do tamanho minimo`() {
        val erro = assertThrows<DomainException> { Senha.de("1234567") }

        assertEquals("A senha deve ter no mínimo 8 caracteres.", erro.message)
    }
}
