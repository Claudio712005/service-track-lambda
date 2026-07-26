package br.com.servicetrack.domain.ports.out

import br.com.servicetrack.domain.model.Usuario
import br.com.servicetrack.domain.vo.Cpf

interface IUsuarioRepositoryPort {

    fun buscarPorCpf(cpf: Cpf): Usuario?
}
