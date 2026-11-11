package br.com.servicetrack.infrastructure.adapter.usuarios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VerificacaoDTOSerializacaoTest {

    @Test
    fun `requisicao sai com documento e senha`() {
        val corpo = Json.encodeToString(VerificacaoReqDTO(documento = "52998224725", senha = "servicetrack-demo"))

        val campos = Json.parseToJsonElement(corpo).jsonObject
        assertEquals(setOf("documento", "senha"), campos.keys)
        assertEquals("52998224725", campos.getValue("documento").jsonPrimitive.content)
    }

    @Test
    fun `resposta do servico de usuarios e lida`() {
        val resposta = """
            {"id":"9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22","documento":"52998224725","nome":"Joana Ferreira Lima","tipoDeUsuario":"CLIENTE","roles":["CLIENTE"]}
        """.trimIndent()

        val dto = Json.decodeFromString<VerificacaoResDTO>(resposta)

        assertEquals("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22", dto.id)
        assertEquals(setOf("CLIENTE"), dto.roles)
    }
}
