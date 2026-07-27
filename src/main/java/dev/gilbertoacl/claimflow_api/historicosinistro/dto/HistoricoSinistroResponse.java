package dev.gilbertoacl.claimflow_api.historicosinistro.dto;

import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoSinistroResponse(
        UUID id,
        String numeroSinistro,
        StatusSinistro statusSinistro,
        UUID responsavelId,
        String observacao,
        LocalDateTime dataAlteracao
) {
}
