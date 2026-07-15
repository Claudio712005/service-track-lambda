package br.com.servicetrack.domain.ports.out

import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.vo.Email

interface IUsuarioRepositoryPort {

    fun buscarPorEmail(email: Email): Usuario?
}
