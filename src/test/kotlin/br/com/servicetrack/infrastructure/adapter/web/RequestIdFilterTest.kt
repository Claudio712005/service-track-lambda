package br.com.servicetrack.infrastructure.adapter.web

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.UriInfo
import org.jboss.logging.MDC
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.UUID

class RequestIdFilterTest {

    private val filtro = RequestIdFilter()

    private inline fun <reified T> fake(crossinline responder: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
            InvocationHandler { _, metodo, argumentos -> responder(metodo.name, argumentos ?: emptyArray()) },
        ) as T

    private val uriInfo: UriInfo = fake { nome, _ ->
        if (nome == "getPath") "autenticacao" else throw UnsupportedOperationException(nome)
    }

    private inner class Requisicao(private val cabecalho: String?) {
        val propriedades = mutableMapOf<String, Any?>()

        val contexto: ContainerRequestContext = fake { nome, argumentos ->
            when (nome) {
                "getHeaderString" -> cabecalho
                "setProperty" -> propriedades.put(argumentos[0] as String, argumentos[1]).let { null }
                "getProperty" -> propriedades[argumentos[0] as String]
                "getMethod" -> "POST"
                "getUriInfo" -> uriInfo
                else -> throw UnsupportedOperationException(nome)
            }
        }
    }

    private inner class Resposta {
        val cabecalhos = MultivaluedHashMap<String, Any>()

        val contexto: ContainerResponseContext = fake { nome, _ ->
            when (nome) {
                "getHeaders" -> cabecalhos
                "getStatus" -> 200
                else -> throw UnsupportedOperationException(nome)
            }
        }
    }

    @AfterEach
    fun limparMdc() {
        MDC.remove(RequestIdFilter.MDC_KEY)
    }

    @Test
    fun `reaproveita o request id recebido no cabecalho`() {
        val requisicao = Requisicao("req-do-gateway")

        filtro.filter(requisicao.contexto)

        assertEquals("req-do-gateway", MDC.get(RequestIdFilter.MDC_KEY))
        assertEquals("req-do-gateway", requisicao.propriedades[RequestIdFilter.MDC_KEY])
    }

    @Test
    fun `gera request id quando o cabecalho nao vem`() {
        val requisicao = Requisicao(null)

        filtro.filter(requisicao.contexto)

        val gerado = requisicao.propriedades[RequestIdFilter.MDC_KEY] as String
        assertEquals(gerado, UUID.fromString(gerado).toString())
        assertEquals(gerado, MDC.get(RequestIdFilter.MDC_KEY))
    }

    @Test
    fun `gera request id quando o cabecalho vem em branco`() {
        val requisicao = Requisicao("  ")

        filtro.filter(requisicao.contexto)

        val gerado = requisicao.propriedades[RequestIdFilter.MDC_KEY] as String
        assertEquals(gerado, UUID.fromString(gerado).toString())
    }

    @Test
    fun `devolve o request id na resposta e limpa o mdc`() {
        val requisicao = Requisicao("req-789")
        val resposta = Resposta()

        filtro.filter(requisicao.contexto)
        filtro.filter(requisicao.contexto, resposta.contexto)

        assertEquals("req-789", resposta.cabecalhos.getFirst(RequestIdFilter.HEADER))
        assertNull(MDC.get(RequestIdFilter.MDC_KEY))
    }

    @Test
    fun `nao escreve cabecalho quando a requisicao nao passou pelo filtro de entrada`() {
        val resposta = Resposta()

        filtro.filter(Requisicao(null).contexto, resposta.contexto)

        assertNull(resposta.cabecalhos.getFirst(RequestIdFilter.HEADER))
    }
}
