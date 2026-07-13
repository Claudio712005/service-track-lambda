package br.com.servicetrack.infrastructure.persistence

import br.com.servicetrack.domain.ports.out.IUsuarioRepositoryPort
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UsuarioRepository: IUsuarioRepositoryPort {

    override fun buscarUsuarioPorEmail(email: String): Boolean {

        return true
    }
}