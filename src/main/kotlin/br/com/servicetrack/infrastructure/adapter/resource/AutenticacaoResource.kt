package br.com.servicetrack.infrastructure.adapter.resource

import br.com.servicetrack.application.dto.request.LoginReqDTO
import br.com.servicetrack.application.dto.response.LoginResDTO
import br.com.servicetrack.domain.ports.`in`.IAutenticacaoUseCase
import br.com.servicetrack.domain.vo.Email
import br.com.servicetrack.domain.vo.Senha
import jakarta.annotation.security.PermitAll
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.jboss.logging.Logger

@Path("autenticacao")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AutenticacaoResource(
    private val autenticacaoUseCase: IAutenticacaoUseCase
) {

    private val log = Logger.getLogger(AutenticacaoResource::class.java)

    @PermitAll
    @POST
    @Path("login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna um token JWT")
    fun autenticar(@Valid requisicao: LoginReqDTO): LoginResDTO {
        log.info("Requisição de login recebida")
        val token = autenticacaoUseCase.autenticar(
            email = Email(requisicao.email),
            senha = Senha.de(requisicao.senha)
        )
        return LoginResDTO.de(token)
    }
}
