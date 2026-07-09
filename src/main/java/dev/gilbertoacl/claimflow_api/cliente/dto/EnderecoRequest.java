package dev.gilbertoacl.claimflow_api.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
    @NotBlank String logradouro,
    @NotBlank String numero,
    String complemento,
    @NotBlank String bairro,
    @NotBlank String cidade,
    @NotBlank String estado,
    @NotBlank @Pattern(regexp = "\\d{5}-\\d{3}") String cep
) {
}
