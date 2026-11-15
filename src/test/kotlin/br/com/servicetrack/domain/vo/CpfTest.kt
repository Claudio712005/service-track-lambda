package br.com.servicetrack.domain.vo

import br.com.servicetrack.domain.exception.DomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CpfTest {

    @Test
    fun `aceita documento valido sem pontuacao`() {
        assertEquals("52998224725", Cpf.de("52998224725").valor)
    }

    @Test
    fun `remove a pontuacao do documento`() {
        assertEquals("52998224725", Cpf.de("529.982.247-25").valor)
    }

    @Test
    fun `aceita documento cujo resto do digito e menor que dois`() {
        assertEquals("19028736530", Cpf.de("190.287.365-30").valor)
    }

    @Test
    fun `rejeita documento com menos de onze digitos`() {
        val erro = assertThrows<DomainException> { Cpf.de("5299822472") }

        assertEquals("CPF deve conter 11 dígitos", erro.message)
    }

    @Test
    fun `rejeita documento com mais de onze digitos`() {
        assertThrows<DomainException> { Cpf.de("529982247251") }
    }

    @Test
    fun `rejeita documento com segundo digito verificador errado`() {
        val erro = assertThrows<DomainException> { Cpf.de("52998224726") }

        assertEquals("CPF inválido", erro.message)
    }

    @Test
    fun `rejeita documento com primeiro digito verificador errado`() {
        assertThrows<DomainException> { Cpf.de("52998224715") }
    }
}
