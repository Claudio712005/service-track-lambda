package br.com.servicetrack.infrastructure.persistence

import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import br.com.servicetrack.domain.vo.Cpf
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class UsuarioRepository : IUsuarioRepositoryPort {

    private val log = Logger.getLogger(UsuarioRepository::class.java)

    override fun buscarPorCpf(cpf: Cpf): Usuario? {
        log.debugf("Consultando usuário por CPF no banco")
        return UsuarioEntity
            .find("cpf", cpf.valor)
            .firstResult()
            ?.paraDominio()
    }
}
