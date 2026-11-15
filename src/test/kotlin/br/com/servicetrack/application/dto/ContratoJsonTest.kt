package br.com.servicetrack.application.dto

import br.com.servicetrack.application.dto.request.LoginReqDTO
import br.com.servicetrack.application.dto.response.ErroResDTO
import br.com.servicetrack.application.dto.response.LoginResDTO
import br.com.servicetrack.domain.model.TokenAutenticacao
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ContratoJsonTest {

    @Test
    fun `login aceita cpf e senha`() {
        val requisicao = Json.decodeFromString<LoginReqDTO>("""{"cpf":"529.982.247-25","senha":"servicetrack-demo"}""")

        assertEquals(LoginReqDTO(cpf = "529.982.247-25", senha = "servicetrack-demo"), requisicao)
    }

    @Test
    fun `login sem senha e recusado na leitura`() {
        assertThrows<SerializationException> { Json.decodeFromString<LoginReqDTO>("""{"cpf":"52998224725"}""") }
    }

    @Test
    fun `resposta de login sai com token, tipo e expiracao`() {
        val resposta = LoginResDTO.de(TokenAutenticacao(token = "jwt-assinado", expiraEmSegundos = 3600))

        val campos = Json.parseToJsonElement(Json.encodeToString(resposta)).jsonObject

        assertEquals(setOf("token", "tipo", "expiraEmSegundos"), campos.keys)
        assertEquals("Bearer", campos.getValue("tipo").jsonPrimitive.content)
        assertEquals(resposta, Json.decodeFromString<LoginResDTO>(Json.encodeToString(resposta)))
    }

    @Test
    fun `resposta de login sem token e recusada na leitura`() {
        assertThrows<SerializationException> {
            Json.decodeFromString<LoginResDTO>("""{"tipo":"Bearer","expiraEmSegundos":3600}""")
        }
    }

    @Test
    fun `erro sai com mensagem e request id nulo quando nao ha rastreio`() {
        val campos = Json.parseToJsonElement(Json.encodeToString(ErroResDTO("Credenciais inválidas", null))).jsonObject

        assertEquals("Credenciais inválidas", campos.getValue("mensagem").jsonPrimitive.content)
        assertEquals(JsonNull, campos.getValue("requestId"))
    }

    @Test
    fun `erro e lido de volta com o request id`() {
        val erro = ErroResDTO("CPF inválido", "req-123")

        assertEquals(erro, Json.decodeFromString<ErroResDTO>(Json.encodeToString(erro)))
    }

    @Test
    fun `erro sem mensagem e recusado na leitura`() {
        assertThrows<SerializationException> { Json.decodeFromString<ErroResDTO>("""{"requestId":"req-1"}""") }
    }
}
