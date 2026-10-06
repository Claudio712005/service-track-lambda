package br.com.servicetrack.application.service

import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.ports.`in`.IAutenticacaoUseCase
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import br.com.servicetrack.domain.ports.out.IVerificadorDeCredencialPort
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class AutenticacaoService(
    private val verificadorDeCredencial: IVerificadorDeCredencialPort,
    private val tokenProvider: ITokenProviderPort,
) : IAutenticacaoUseCase {

    private val log = Logger.getLogger(AutenticacaoService::class.java)

    override fun autenticar(cpf: Cpf, senha: Senha): TokenAutenticacao {
        log.infof("Iniciando autenticacao para documento=%s", mascarar(cpf.valor))

        val identidade = verificadorDeCredencial.verificar(cpf, senha)
        val token = tokenProvider.gerar(identidade)

        log.infof("Autenticacao concluida com sucesso para usuarioId=%s", identidade.id.valor)
        return token
    }

    private fun mascarar(documento: String): String =
        "*".repeat(documento.length - 2) + documento.takeLast(2)
}
