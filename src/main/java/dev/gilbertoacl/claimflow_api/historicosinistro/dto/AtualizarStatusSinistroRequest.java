package dev.gilbertoacl.claimflow_api.historicosinistro.dto;

import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AtualizarStatusSinistroRequest(
        @NotNull String numeroSinistro,
        @NotNull StatusSinistro novoStatusSinistro,
        @NotNull UUID responsavelId,
        String observacao
) {
}
