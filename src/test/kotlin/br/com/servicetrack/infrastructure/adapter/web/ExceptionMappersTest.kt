package br.com.servicetrack.infrastructure.adapter.web

import br.com.servicetrack.application.dto.response.ErroResDTO
import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.exception.ServicoDeUsuariosIndisponivelException
import org.jboss.logging.MDC
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExceptionMappersTest {

    @AfterEach
    fun limparMdc() {
        MDC.remove(RequestIdFilter.MDC_KEY)
    }

    @Test
    fun `credencial invalida responde 401 com o request id`() {
        MDC.put(RequestIdFilter.MDC_KEY, "req-123")

        val resposta = CredenciaisInvalidasMapper().toResponse(CredenciaisInvalidasException())

        assertEquals(401, resposta.status)
        assertEquals(ErroResDTO("Credenciais inválidas", "req-123"), resposta.entity)
    }

    @Test
    fun `erro de dominio responde 400 com a mensagem do dominio`() {
        val resposta = DomainExceptionMapper().toResponse(DomainException("CPF inválido"))

        assertEquals(400, resposta.status)
        assertEquals(ErroResDTO("CPF inválido", null), resposta.entity)
    }

    @Test
    fun `servico de usuarios indisponivel responde 503`() {
        MDC.put(RequestIdFilter.MDC_KEY, "req-456")

        val resposta = ServicoDeUsuariosIndisponivelMapper()
            .toResponse(ServicoDeUsuariosIndisponivelException(causa = IllegalStateException("fora do ar")))

        assertEquals(503, resposta.status)
        assertEquals(ErroResDTO("Servico de usuarios indisponivel", "req-456"), resposta.entity)
    }
}
