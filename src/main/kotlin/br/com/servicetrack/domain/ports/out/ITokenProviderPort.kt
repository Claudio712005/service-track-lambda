package br.com.servicetrack.domain.ports.out

import br.com.servicetrack.domain.model.IdentidadeAutenticada
import br.com.servicetrack.domain.model.TokenAutenticacao

interface ITokenProviderPort {
    fun gerar(identidade: IdentidadeAutenticada): TokenAutenticacao
}
