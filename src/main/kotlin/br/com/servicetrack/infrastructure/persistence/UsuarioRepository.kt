package br.com.servicetrack.infrastructure.persistence

import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import br.com.servicetrack.domain.vo.Email
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class UsuarioRepository : IUsuarioRepositoryPort {

    private val log = Logger.getLogger(UsuarioRepository::class.java)

    override fun buscarPorEmail(email: Email): Usuario? {
        log.debugf("Consultando usuário por email no banco")
        return UsuarioEntity
            .find("email", email.valor)
            .firstResult()
            ?.paraDominio()
    }
}
