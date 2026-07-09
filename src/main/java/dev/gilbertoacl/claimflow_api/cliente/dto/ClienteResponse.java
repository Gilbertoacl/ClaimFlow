package dev.gilbertoacl.claimflow_api.cliente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        String telefone,
        EnderecoResponse endereco,
        LocalDate dataNascimento,
        LocalDateTime dataCadastro
) {
}
