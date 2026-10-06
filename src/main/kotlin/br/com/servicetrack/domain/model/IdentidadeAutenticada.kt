package br.com.servicetrack.domain.model

import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.UsuarioId

class IdentidadeAutenticada(
    val id: UsuarioId,
    val documento: Cpf,
    val nome: String,
    val roles: Set<String>,
)
