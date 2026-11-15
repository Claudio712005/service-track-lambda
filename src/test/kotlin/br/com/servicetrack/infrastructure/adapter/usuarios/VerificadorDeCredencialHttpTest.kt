package br.com.servicetrack.infrastructure.adapter.usuarios

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.ServicoDeUsuariosIndisponivelException
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VerificadorDeCredencialHttpTest {

    private val cpf = Cpf.de("52998224725")
    private val senha = Senha.de("servicetrack-demo")

    private val respostaDoServico = VerificacaoResDTO(
        id = "9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22",
        documento = "529.982.247-25",
        nome = "Joana Ferreira Lima",
        tipoDeUsuario = "CLIENTE",
        roles = setOf("CLIENTE"),
    )

    private fun verificador(resposta: (VerificacaoReqDTO) -> VerificacaoResDTO) =
        VerificadorDeCredencialHttp(object : UsuariosRestClient {
            override fun verificar(requisicao: VerificacaoReqDTO) = resposta(requisicao)
        })

    @Test
    fun `envia documento e senha e monta a identidade com a resposta`() {
        var enviado: VerificacaoReqDTO? = null

        val identidade = verificador {
            enviado = it
            respostaDoServico
        }.verificar(cpf, senha)

        assertEquals(VerificacaoReqDTO(documento = "52998224725", senha = "servicetrack-demo"), enviado)
        assertEquals("9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22", identidade.id.valor)
        assertEquals("52998224725", identidade.documento.valor)
        assertEquals("Joana Ferreira Lima", identidade.nome)
        assertEquals(setOf("CLIENTE"), identidade.roles)
    }

    @ParameterizedTest
    @ValueSource(ints = [401, 404])
    fun `traduz recusa do servico de usuarios em credencial invalida`(status: Int) {
        val erro = assertThrows<CredenciaisInvalidasException> {
            verificador { throw WebApplicationException(status) }.verificar(cpf, senha)
        }

        assertEquals("Credenciais inválidas", erro.message)
    }

    @Test
    fun `traduz limite de requisicoes em indisponibilidade com a causa`() {
        val recusa = WebApplicationException(429)

        val erro = assertThrows<ServicoDeUsuariosIndisponivelException> {
            verificador { throw recusa }.verificar(cpf, senha)
        }

        assertEquals("Servico de usuarios sob limite de requisicoes", erro.message)
        assertSame(recusa, erro.cause)
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 500, 503])
    fun `traduz status inesperado em indisponibilidade`(status: Int) {
        val erro = assertThrows<ServicoDeUsuariosIndisponivelException> {
            verificador { throw WebApplicationException(status) }.verificar(cpf, senha)
        }

        assertEquals("Servico de usuarios indisponivel", erro.message)
        assertEquals(status, (erro.cause as WebApplicationException).response.status)
    }

    @Test
    fun `traduz falha de rede em indisponibilidade com a causa`() {
        val falha = ProcessingException("connect timed out")

        val erro = assertThrows<ServicoDeUsuariosIndisponivelException> {
            verificador { throw falha }.verificar(cpf, senha)
        }

        assertSame(falha, erro.cause)
    }
}
