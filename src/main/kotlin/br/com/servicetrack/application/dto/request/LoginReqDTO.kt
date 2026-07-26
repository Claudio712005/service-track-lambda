package br.com.servicetrack.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import kotlinx.serialization.Serializable
import org.hibernate.validator.constraints.Length

@Serializable
data class LoginReqDTO(

    @Pattern(
        regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}",
        message = "O CPF deve conter 11 dígitos, com ou sem pontuação."
    )
    @NotBlank(message = "O CPF não pode ser nulo ou vazio.")
    val cpf: String,

    @Length(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @NotBlank(message = "A senha não pode ser nula ou vazia.")
    val senha: String
)
