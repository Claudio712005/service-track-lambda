package br.com.servicetrack.domain.model

import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.UsuarioId

class Usuario private constructor(
    val id: UsuarioId,
    val cpf: Cpf
){
    companion object {
        fun de(id: UsuarioId, cpf: Cpf) = Usuario(id, cpf)
    }
}
