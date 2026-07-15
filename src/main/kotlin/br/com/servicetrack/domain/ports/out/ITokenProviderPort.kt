package br.com.servicetrack.domain.ports.out

import br.com.servicetrack.domain.model.TokenAutenticacao
import br.com.servicetrack.domain.model.Usuario

interface ITokenProviderPort {

    fun gerar(usuario: Usuario): TokenAutenticacao
}
