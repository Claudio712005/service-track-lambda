package br.com.servicetrack.infrastructure.adapter.usuarios

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "usuarios")
@Path("/credenciais/verificacao")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
interface UsuariosRestClient {

    @POST
    fun verificar(requisicao: VerificacaoReqDTO): VerificacaoResDTO
}
