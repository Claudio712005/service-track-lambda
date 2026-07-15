package br.com.servicetrack.application.service

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
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

    private val email = Email("usuario@servicetrack.com.br")
    private val senha = Senha.de("senhaForte123")

    private fun usuario(ativo: Boolean = true) = Usuario.restaurar(
        id = UsuarioId.gerar(),
        cpf = Cpf("52998224725"),
        email = email,
        senhaHash = "hash-armazenado",
        roles = setOf(Role.CLIENTE),
        ativo = ativo
    )

    private fun repo(usuario: Usuario?) = object : IUsuarioRepositoryPort {
        override fun buscarPorEmail(email: Email) = usuario
    }

    private val tokenProvider = object : ITokenProviderPort {
        override fun gerar(usuario: Usuario) =
            TokenAutenticacao(token = "jwt-fake", expiraEmSegundos = 3600)
    }

    @Test
    fun `emite token quando credenciais sao validas`() {
        val service = AutenticacaoService(
            usuarioRepository = repo(usuario()),
            tokenProvider = tokenProvider,
            verificadorSenha = VerificadorSenha { _, _ -> true }
        )

        val token = service.autenticar(email, senha)

        assertEquals("jwt-fake", token.token)
        assertEquals("Bearer", token.tipo)
    }

    @Test
    fun `falha quando senha nao confere`() {
        val service = AutenticacaoService(
            usuarioRepository = repo(usuario()),
            tokenProvider = tokenProvider,
            verificadorSenha = VerificadorSenha { _, _ -> false }
        )

        assertThrows(CredenciaisInvalidasException::class.java) {
            service.autenticar(email, senha)
        }
    }

    @Test
    fun `falha quando usuario nao existe`() {
        val service = AutenticacaoService(
            usuarioRepository = repo(null),
            tokenProvider = tokenProvider,
            verificadorSenha = VerificadorSenha { _, _ -> true }
        )

        assertThrows(CredenciaisInvalidasException::class.java) {
            service.autenticar(email, senha)
        }
    }

    @Test
    fun `falha quando usuario esta inativo`() {
        val service = AutenticacaoService(
            usuarioRepository = repo(usuario(ativo = false)),
            tokenProvider = tokenProvider,
            verificadorSenha = VerificadorSenha { _, _ -> true }
        )

        assertThrows(CredenciaisInvalidasException::class.java) {
            service.autenticar(email, senha)
        }
    }
}
