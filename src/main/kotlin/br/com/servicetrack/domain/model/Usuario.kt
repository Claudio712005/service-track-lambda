package br.com.servicetrack.domain.model

import br.com.servicetrack.domain.exception.CredenciaisInvalidasException
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Email
import br.com.servicetrack.domain.vo.Senha
import br.com.servicetrack.domain.vo.UsuarioId

fun interface VerificadorSenha {
    fun confere(senha: Senha, hash: String): Boolean
}

class Usuario private constructor(
    val id: UsuarioId,
    val cpf: Cpf,
    val email: Email,
    private val senhaHash: String,
    val roles: Set<Role>,
    val ativo: Boolean
) {

    fun autenticar(senha: Senha, verificador: VerificadorSenha) {
        if (!ativo) {
            throw CredenciaisInvalidasException()
        }
        if (!verificador.confere(senha, senhaHash)) {
            throw CredenciaisInvalidasException()
        }
    }

    companion object {
        fun restaurar(
            id: UsuarioId,
            cpf: Cpf,
            email: Email,
            senhaHash: String,
            roles: Set<Role>,
            ativo: Boolean
        ) = Usuario(id, cpf, email, senhaHash, roles, ativo)
    }
}
