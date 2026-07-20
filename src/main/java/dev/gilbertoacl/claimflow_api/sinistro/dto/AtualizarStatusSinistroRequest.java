package dev.gilbertoacl.claimflow_api.sinistro.dto;

import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusSinistroRequest(
        @NotNull String numeroSinistro,
        @NotNull StatusSinistro novoStatusSinistro,
        String observacao
) {
}
