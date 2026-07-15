package br.com.servicetrack.infrastructure.adapter.security

import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.ports.out.ITokenProviderPort
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration

@ApplicationScoped
class JwtTokenProvider(
    @param:ConfigProperty(name = "mp.jwt.verify.issuer")
    private val issuer: String,
    @param:ConfigProperty(name = "servicetrack.jwt.expiracao-segundos", defaultValue = "3600")
    private val expiracaoSegundos: Long
) : ITokenProviderPort {

    private val log = Logger.getLogger(JwtTokenProvider::class.java)

    override fun gerar(usuario: Usuario): TokenAutenticacao {
        val token = Jwt.issuer(issuer)
            .subject(usuario.id.valor)
            .upn(usuario.email.valor)
            .groups(usuario.roles.map { it.name }.toSet())
            .claim("cpf", usuario.cpf.valor)
            .expiresIn(Duration.ofSeconds(expiracaoSegundos))
            .sign()

        log.debugf("JWT emitido para usuarioId=%s (expira em %ds)", usuario.id.valor, expiracaoSegundos)
        return TokenAutenticacao(token = token, expiraEmSegundos = expiracaoSegundos)
    }
}
