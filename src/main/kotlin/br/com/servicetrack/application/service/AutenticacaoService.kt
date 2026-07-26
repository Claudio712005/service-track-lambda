package br.com.servicetrack.application.service

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.model.VerificadorSenha
import br.com.servicetrack.domain.ports.`in`.IAutenticacaoUseCase
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import br.com.servicetrack.domain.vo.Cpf
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

    override fun autenticar(cpf: Cpf, senha: Senha): TokenAutenticacao {
        log.infof("Iniciando autenticação para cpf=%s", mascarar(cpf.valor))

        val usuario = usuarioRepository.buscarPorCpf(cpf)
        if (usuario == null) {
            log.warnf("Falha de autenticação: usuário não encontrado para cpf=%s", mascarar(cpf.valor))
            throw CredenciaisInvalidasException()
        }

        usuario.autenticar(senha, verificadorSenha)

        val token = tokenProvider.gerar(usuario)
        log.infof("Autenticação concluída com sucesso para usuarioId=%s", usuario.id.valor)
        return token
    }

    private fun mascarar(cpf: String): String =
        "*".repeat(cpf.length - 2) + cpf.takeLast(2)
}
