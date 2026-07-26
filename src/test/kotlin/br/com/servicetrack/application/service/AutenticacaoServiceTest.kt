package br.com.servicetrack.application.service

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.model.VerificadorSenha
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Email
import br.com.servicetrack.domain.vo.Senha
import br.com.servicetrack.domain.vo.UsuarioId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AutenticacaoServiceTest {

    private val cpf = Cpf.de("52998224725")
    private val senha = Senha.de("senhaForte123")

    private fun usuario(ativo: Boolean = true) = Usuario.restaurar(
        id = UsuarioId.gerar(),
        cpf = cpf,
        email = Email("usuario@servicetrack.com.br"),
        senhaHash = "hash-armazenado",
        roles = setOf(Role.CLIENTE),
        ativo = ativo
    )

    private fun repo(usuario: Usuario?) = object : IUsuarioRepositoryPort {
        override fun buscarPorCpf(cpf: Cpf) = usuario
    }

    private val tokenProvider = object : ITokenProviderPort {
        override fun gerar(usuario: Usuario) =
            TokenAutenticacao(token = "jwt-fake", expiraEmSegundos = 3600)
    }

    private fun service(usuario: Usuario?, senhaConfere: Boolean) = AutenticacaoService(
        usuarioRepository = repo(usuario),
        tokenProvider = tokenProvider,
        verificadorSenha = VerificadorSenha { _, _ -> senhaConfere }
    )

    @Test
    fun `emite token quando credenciais sao validas`() {
        val token = service(usuario(), senhaConfere = true).autenticar(cpf, senha)

        assertEquals("jwt-fake", token.token)
        assertEquals("Bearer", token.tipo)
    }

    @Test
    fun `falha quando senha nao confere`() {
        assertThrows(CredenciaisInvalidasException::class.java) {
            service(usuario(), senhaConfere = false).autenticar(cpf, senha)
        }
    }

    @Test
    fun `falha quando cpf nao existe`() {
        assertThrows(CredenciaisInvalidasException::class.java) {
            service(null, senhaConfere = true).autenticar(cpf, senha)
        }
    }

    @Test
    fun `falha quando usuario esta inativo`() {
        assertThrows(CredenciaisInvalidasException::class.java) {
            service(usuario(ativo = false), senhaConfere = true).autenticar(cpf, senha)
        }
    }

    @Test
    fun `normaliza cpf com pontuacao`() {
        assertEquals("52998224725", Cpf.de("529.982.247-25").valor)
    }

    @Test
    fun `rejeita cpf com digito verificador invalido`() {
        assertThrows(DomainException::class.java) {
            Cpf.de("11144477088")
        }
    }

    @Test
    fun `rejeita cpf com digitos repetidos`() {
        assertThrows(DomainException::class.java) {
            Cpf.de("11111111111")
        }
    }
}
