package br.com.servicetrack.domain.vo

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Cpf private constructor(val valor: String) {

    companion object {

        private const val TAMANHO = 11

        fun de(bruto: String): Cpf {
            val numeros = bruto.filter { it.isDigit() }

            if (numeros.length != TAMANHO) {
                throw DomainException("CPF deve conter $TAMANHO dígitos")
            }

            if (numeros.all { it == numeros[0] }) {
                throw DomainException("CPF inválido")
            }

            if (!digitosConferem(numeros)) {
                throw DomainException("CPF inválido")
            }

            return Cpf(numeros)
        }

        private fun digitosConferem(cpf: String): Boolean {
            val primeiro = calcularDigito(cpf.substring(0, 9), 10)
            val segundo = calcularDigito(cpf.substring(0, 10), 11)
            return cpf[9].digitToInt() == primeiro && cpf[10].digitToInt() == segundo
        }

        private fun calcularDigito(base: String, pesoInicial: Int): Int {
            val soma = base.mapIndexed { indice, caractere ->
                caractere.digitToInt() * (pesoInicial - indice)
            }.sum()

            val resto = soma % 11
            return if (resto < 2) 0 else 11 - resto
        }
    }
}
