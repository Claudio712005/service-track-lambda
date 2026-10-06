package br.com.servicetrack.infrastructure.adapter.usuarios

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.ServicoDeUsuariosIndisponivelException
import br.com.servicetrack.domain.model.IdentidadeAutenticada
import br.com.servicetrack.domain.ports.out.IVerificadorDeCredencialPort
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha
import br.com.servicetrack.domain.vo.UsuarioId
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class VerificadorDeCredencialHttp(
    @param:RestClient private val cliente: UsuariosRestClient,
) : IVerificadorDeCredencialPort {

    private val log = Logger.getLogger(VerificadorDeCredencialHttp::class.java)

    override fun verificar(documento: Cpf, senha: Senha): IdentidadeAutenticada {
        val resposta = try {
            cliente.verificar(VerificacaoReqDTO(documento = documento.valor, senha = senha.valor))
        } catch (erro: WebApplicationException) {
            throw traduzir(erro)
        } catch (erro: ProcessingException) {
            log.warnf("Falha de rede ao chamar o servico de usuarios: %s", erro.message)
            throw ServicoDeUsuariosIndisponivelException(causa = erro)
        }

        return IdentidadeAutenticada(
            id = UsuarioId.de(resposta.id),
            documento = Cpf.de(resposta.documento),
            nome = resposta.nome,
            roles = resposta.roles,
        )
    }

    private fun traduzir(erro: WebApplicationException): RuntimeException {
        val status = erro.response.status
        return when {
            status == Response.Status.UNAUTHORIZED.statusCode ||
                status == Response.Status.NOT_FOUND.statusCode -> CredenciaisInvalidasException()

            status == TOO_MANY_REQUESTS -> {
                log.warn("Servico de usuarios recusou por limite de requisicoes")
                ServicoDeUsuariosIndisponivelException("Servico de usuarios sob limite de requisicoes", erro)
            }

            else -> {
                log.warnf("Servico de usuarios respondeu status inesperado: %d", status)
                ServicoDeUsuariosIndisponivelException(causa = erro)
            }
        }
    }

    private companion object {
        const val TOO_MANY_REQUESTS = 429
    }
}
