package br.com.servicetrack.resource

import br.com.servicetrack.ports.IAutenticacaoResourcePort
import jakarta.annotation.security.PermitAll
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.openapi.annotations.Operation

@ApplicationScoped
@Path("autenticacao")
class AutenticacaoResource(

): IAutenticacaoResourcePort {

    @PermitAll
    @POST
    @Path("login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna um token JWT")
    override fun autenticar(

    ): Void {
        TODO("Not yet implemented")
    }
}