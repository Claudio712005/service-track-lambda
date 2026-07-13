package br.com.servicetrack.domain.vo

@JvmInline
value class Senha private constructor(
    val valor: String
){
    init {

        require(valor.length >= 8) { "A senha deve ter no mínimo 8 caracteres." }
    }

    companion object {

    }
}