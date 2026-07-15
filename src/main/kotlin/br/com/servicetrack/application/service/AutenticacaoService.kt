package br.com.servicetrack.application.service

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.model.VerificadorSenha
import br.com.servicetrack.domain.ports.`in`.IAutenticacaoUseCase
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import br.com.servicetrack.domain.vo.Email
import br.com.servicetrack.domain.vo.Senha
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class AutenticacaoService(
    private val usuarioRepository: IUsuarioRepositoryPort,
    private val tokenProvider: ITokenProviderPort,
    private val verificadorSenha: VerificadorSenha
) : IAutenticacaoUseCase {

    private val log = Logger.getLogger(AutenticacaoService::class.java)

    override fun autenticar(email: Email, senha: Senha): TokenAutenticacao {
        log.infof("Iniciando autenticação para email=%s", mascarar(email.valor))

        val usuario = usuarioRepository.buscarPorEmail(email)
        if (usuario == null) {
            log.warnf("Falha de autenticação: usuário não encontrado para email=%s", mascarar(email.valor))
            throw CredenciaisInvalidasException()
        }

        usuario.autenticar(senha, verificadorSenha)

        val token = tokenProvider.gerar(usuario)
        log.infof("Autenticação concluída com sucesso para usuarioId=%s", usuario.id.valor)
        return token
    }

    private fun mascarar(email: String): String {
        val at = email.indexOf('@')
        if (at <= 1) return "***"
        return "${email.first()}***${email.substring(at)}"
    }
}
