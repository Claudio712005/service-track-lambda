package br.com.servicetrack.domain.ports.`in`

import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.vo.Email
import br.com.servicetrack.domain.vo.Senha

interface IAutenticacaoUseCase {


    fun autenticar(email: Email, senha: Senha): TokenAutenticacao
}
