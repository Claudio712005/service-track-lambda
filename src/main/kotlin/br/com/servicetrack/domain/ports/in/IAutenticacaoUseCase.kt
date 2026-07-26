package br.com.servicetrack.domain.ports.`in`

import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha

interface IAutenticacaoUseCase {

    fun autenticar(cpf: Cpf, senha: Senha): TokenAutenticacao
}
