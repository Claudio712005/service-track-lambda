package br.com.servicetrack.application.service

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.exception.ServicoDeUsuariosIndisponivelException
import br.com.servicetrack.domain.model.IdentidadeAutenticada
import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import br.com.servicetrack.domain.ports.out.IVerificadorDeCredencialPort
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha
import br.com.servicetrack.domain.vo.UsuarioId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AutenticacaoServiceTest {

    private val cpfValido = "52998224725"

    private fun identidade() = IdentidadeAutenticada(
        id = UsuarioId.gerar(),
        documento = Cpf.de(cpfValido),
        nome = "Joana Ferreira",
        roles = setOf("CLIENTE"),
    )

    private fun verificador(resultado: () -> IdentidadeAutenticada) = object : IVerificadorDeCredencialPort {
        override fun verificar(documento: Cpf, senha: Senha) = resultado()
    }

    private val tokenProvider = object : ITokenProviderPort {
        override fun gerar(identidade: IdentidadeAutenticada) =
            TokenAutenticacao(token = "token-de-teste", expiraEmSegundos = 3600)
    }

    private fun service(resultado: () -> IdentidadeAutenticada) =
        AutenticacaoService(verificadorDeCredencial = verificador(resultado), tokenProvider = tokenProvider)

    @Test
    fun `emite token quando o servico de usuarios confirma a identidade`() {
        val token = service { identidade() }.autenticar(Cpf.de(cpfValido), Senha.de("segredo-forte"))

        assertEquals("token-de-teste", token.token)
        assertEquals(3600, token.expiraEmSegundos)
    }

    @Test
    fun `propaga credencial invalida sem traduzir`() {
        assertThrows(CredenciaisInvalidasException::class.java) {
            service { throw CredenciaisInvalidasException() }
                .autenticar(Cpf.de(cpfValido), Senha.de("senha-errada"))
        }
    }

    @Test
    fun `propaga indisponibilidade do servico de usuarios`() {
        assertThrows(ServicoDeUsuariosIndisponivelException::class.java) {
            service { throw ServicoDeUsuariosIndisponivelException() }
                .autenticar(Cpf.de(cpfValido), Senha.de("segredo-forte"))
        }
    }

    @Test
    fun `normaliza documento com pontuacao antes de perguntar`() {
        var recebido: String? = null
        val espiao = object : IVerificadorDeCredencialPort {
            override fun verificar(documento: Cpf, senha: Senha): IdentidadeAutenticada {
                recebido = documento.valor
                return identidade()
            }
        }

        AutenticacaoService(espiao, tokenProvider).autenticar(Cpf.de("529.982.247-25"), Senha.de("segredo-forte"))

        assertEquals(cpfValido, recebido)
    }

    @Test
    fun `rejeita documento com digito verificador invalido`() {
        assertThrows(DomainException::class.java) { Cpf.de("52998224724") }
    }

    @Test
    fun `rejeita documento com digitos repetidos`() {
        assertThrows(DomainException::class.java) { Cpf.de("11111111111") }
    }
}
