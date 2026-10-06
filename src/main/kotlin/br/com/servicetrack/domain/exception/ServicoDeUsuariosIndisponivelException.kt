package br.com.servicetrack.domain.exception

class ServicoDeUsuariosIndisponivelException(
    mensagem: String = "Servico de usuarios indisponivel",
    causa: Throwable? = null,
) : RuntimeException(mensagem, causa)
