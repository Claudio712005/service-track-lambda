package br.com.servicetrack.infrastructure.adapter.web

import br.com.servicetrack.application.dto.response.ErroResDTO
import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.DomainException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.MDC
import org.jboss.logging.Logger

private fun erro(status: Response.Status, mensagem: String): Response =
    Response.status(status)
        .entity(ErroResDTO(mensagem, MDC.get(RequestIdFilter.MDC_KEY) as? String))
        .build()

@Provider
class CredenciaisInvalidasMapper : ExceptionMapper<CredenciaisInvalidasException> {

    private val log = Logger.getLogger(CredenciaisInvalidasMapper::class.java)

    override fun toResponse(exception: CredenciaisInvalidasException): Response {
        log.warnf("Autenticação negada: %s", exception.message)
        return erro(Response.Status.UNAUTHORIZED, exception.message ?: "Credenciais inválidas")
    }
}

@Provider
class DomainExceptionMapper : ExceptionMapper<DomainException> {

    private val log = Logger.getLogger(DomainExceptionMapper::class.java)

    override fun toResponse(exception: DomainException): Response {
        log.warnf("Requisição inválida: %s", exception.message)
        return erro(Response.Status.BAD_REQUEST, exception.message ?: "Requisição inválida")
    }
}
