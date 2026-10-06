package br.com.servicetrack.domain.ports.out

import br.com.servicetrack.domain.model.IdentidadeAutenticada
import br.com.servicetrack.domain.vo.Cpf
import br.com.servicetrack.domain.vo.Senha

interface IVerificadorDeCredencialPort {
    fun verificar(documento: Cpf, senha: Senha): IdentidadeAutenticada
}
