package br.com.servicetrack.infrastructure.adapter.web

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.MDC
import org.jboss.logging.Logger
import java.util.UUID

@Provider
@Priority(Priorities.AUTHENTICATION - 100)
class RequestIdFilter : ContainerRequestFilter, ContainerResponseFilter {

    private val log = Logger.getLogger(RequestIdFilter::class.java)

    override fun filter(requestContext: ContainerRequestContext) {
        val requestId = requestContext.getHeaderString(HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        MDC.put(MDC_KEY, requestId)
        requestContext.setProperty(MDC_KEY, requestId)
        log.debugf("Início %s %s", requestContext.method, requestContext.uriInfo.path)
    }

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        val requestId = requestContext.getProperty(MDC_KEY) as? String
        if (requestId != null) {
            responseContext.headers.putSingle(HEADER, requestId)
        }
        log.debugf("Fim %s %s -> %d", requestContext.method, requestContext.uriInfo.path, responseContext.status)
        MDC.remove(MDC_KEY)
    }

    companion object {
        const val HEADER = "X-Request-Id"
        const val MDC_KEY = "requestId"
    }
}
