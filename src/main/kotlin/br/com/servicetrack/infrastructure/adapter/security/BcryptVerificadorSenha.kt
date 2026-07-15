package br.com.servicetrack.infrastructure.adapter.security

import br.com.servicetrack.domain.model.VerificadorSenha
import br.com.servicetrack.domain.vo.Senha
import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class BcryptVerificadorSenha : VerificadorSenha {

    override fun confere(senha: Senha, hash: String): Boolean =
        BcryptUtil.matches(senha.valor, hash)
}
