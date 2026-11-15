package br.com.servicetrack.infrastructure.adapter.security

import br.com.servicetrack.domain.model.IdentidadeAutenticada
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.UsuarioId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64

class JwtTokenProviderTest {

    companion object {
        private const val PROPRIEDADE_DA_CHAVE = "smallrye.jwt.sign.key"

        private val par: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

        @JvmStatic
        @BeforeAll
        fun configurarChaveDeAssinatura() {
            System.setProperty(PROPRIEDADE_DA_CHAVE, Base64.getEncoder().encodeToString(par.private.encoded))
        }

        @JvmStatic
        @AfterAll
        fun removerChaveDeAssinatura() {
            System.clearProperty(PROPRIEDADE_DA_CHAVE)
        }
    }

    private val identidade = IdentidadeAutenticada(
        id = UsuarioId.de("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22"),
        documento = Cpf.de("52998224725"),
        nome = "Joana Ferreira Lima",
        roles = setOf("CLIENTE"),
    )

    private fun parte(token: String, indice: Int): JsonObject =
        Json.parseToJsonElement(String(Base64.getUrlDecoder().decode(token.split(".")[indice]))).jsonObject

    @Test
    fun `emite token rs256 com emissor, sujeito, documento e papeis`() {
        val emitido = JwtTokenProvider(issuer = "service-track-api", expiracaoSegundos = 3600).gerar(identidade)

        val cabecalho = parte(emitido.token, 0)
        val corpo = parte(emitido.token, 1)
        assertEquals("RS256", cabecalho.getValue("alg").jsonPrimitive.content)
        assertEquals("service-track-api", corpo.getValue("iss").jsonPrimitive.content)
        assertEquals("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22", corpo.getValue("sub").jsonPrimitive.content)
        assertEquals("52998224725", corpo.getValue("upn").jsonPrimitive.content)
        assertEquals("52998224725", corpo.getValue("cpf").jsonPrimitive.content)
        assertEquals(listOf("CLIENTE"), corpo.getValue("groups").jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun `token expira no prazo configurado`() {
        val emitido = JwtTokenProvider(issuer = "service-track-api", expiracaoSegundos = 900).gerar(identidade)

        val corpo = parte(emitido.token, 1)
        assertEquals(900, corpo.getValue("exp").jsonPrimitive.long - corpo.getValue("iat").jsonPrimitive.long)
        assertEquals(900, emitido.expiraEmSegundos)
        assertEquals("Bearer", emitido.tipo)
    }

    @Test
    fun `assinatura confere com a chave publica do par`() {
        val (cabecalho, corpo, assinatura) = JwtTokenProvider(issuer = "service-track-api", expiracaoSegundos = 3600)
            .gerar(identidade).token.split(".")

        val verificador = Signature.getInstance("SHA256withRSA").apply {
            initVerify(par.public)
            update("$cabecalho.$corpo".toByteArray())
        }

        assertTrue(verificador.verify(Base64.getUrlDecoder().decode(assinatura)))
    }
}
