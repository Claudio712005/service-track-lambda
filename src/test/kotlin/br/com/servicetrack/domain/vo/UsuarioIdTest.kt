package br.com.servicetrack.domain.vo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class UsuarioIdTest {

    @Test
    fun `gera identificadores distintos no formato uuid`() {
        val primeiro = UsuarioId.gerar()
        val segundo = UsuarioId.gerar()

        assertNotEquals(primeiro.valor, segundo.valor)
        assertEquals(primeiro.valor, UUID.fromString(primeiro.valor).toString())
    }

    @Test
    fun `preserva o identificador recebido`() {
        assertEquals("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22", UsuarioId.de("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22").valor)
    }
}
