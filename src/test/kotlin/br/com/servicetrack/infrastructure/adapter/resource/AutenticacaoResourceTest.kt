package br.com.servicetrack.infrastructure.adapter.resource

import br.com.servicetrack.application.dto.request.LoginReqDTO
import br.com.servicetrack.application.dto.response.LoginResDTO
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.ports.`in`.IAutenticacaoUseCase
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AutenticacaoResourceTest {

    private class CasoDeUsoEspiao : IAutenticacaoUseCase {
        var cpfRecebido: String? = null
        var senhaRecebida: String? = null
        var chamado = false

        override fun autenticar(cpf: Cpf, senha: Senha): TokenAutenticacao {
            chamado = true
            cpfRecebido = cpf.valor
            senhaRecebida = senha.valor
            return TokenAutenticacao(token = "jwt-assinado", expiraEmSegundos = 3600)
        }
    }

    @Test
    fun `autentica com o documento normalizado e devolve o token`() {
        val casoDeUso = CasoDeUsoEspiao()

        val resposta = AutenticacaoResource(casoDeUso)
            .autenticar(LoginReqDTO(cpf = "529.982.247-25", senha = "servicetrack-demo"))

        assertEquals(LoginResDTO(token = "jwt-assinado", tipo = "Bearer", expiraEmSegundos = 3600), resposta)
        assertEquals("52998224725", casoDeUso.cpfRecebido)
        assertEquals("servicetrack-demo", casoDeUso.senhaRecebida)
    }

    @Test
    fun `recusa documento invalido sem chamar o caso de uso`() {
        val casoDeUso = CasoDeUsoEspiao()

        assertThrows<DomainException> {
            AutenticacaoResource(casoDeUso).autenticar(LoginReqDTO(cpf = "111.111.111-11", senha = "servicetrack-demo"))
        }
        assertFalse(casoDeUso.chamado)
    }

    @Test
    fun `recusa senha curta sem chamar o caso de uso`() {
        val casoDeUso = CasoDeUsoEspiao()

        assertThrows<DomainException> {
            AutenticacaoResource(casoDeUso).autenticar(LoginReqDTO(cpf = "52998224725", senha = "curta"))
        }
        assertFalse(casoDeUso.chamado)
    }
}
