package dev.gilbertoacl.claimflow_api.cliente.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank String nome,
        @NotBlank @CPF String cpf,
        @NotBlank @Email String email,
        String telefone,
        @Valid EnderecoRequest endereco,
        @Past LocalDate dataNascimento
) {
}
