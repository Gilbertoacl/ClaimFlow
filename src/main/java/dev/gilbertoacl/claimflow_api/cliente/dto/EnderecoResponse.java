package dev.gilbertoacl.claimflow_api.cliente.dto;

public record EnderecoResponse(
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String estado,
    String cep
) {
}
