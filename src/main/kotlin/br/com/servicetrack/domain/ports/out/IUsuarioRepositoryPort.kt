package br.com.servicetrack.domain.ports.out

interface IUsuarioRepositoryPort {

    fun buscarUsuarioPorEmail(email: String): Boolean
}