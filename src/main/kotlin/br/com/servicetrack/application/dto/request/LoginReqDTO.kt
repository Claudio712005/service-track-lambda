package br.com.servicetrack.application.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable
import org.hibernate.validator.constraints.Length

@Serializable
data class LoginReqDTO(

    @Length(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @NotBlank(message = "A senha não pode ser nula ou vazia.")
    val senha: String,

    @Email(message = "O email deve ser válido.")
    @NotBlank(message = "O email não pode ser nulo ou vazio.")
    val email: String
)
