package dev.gilbertoacl.claimflow_api.sinistro.dto;

import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SinistroResponse(
        UUID id,
        UUID apoliceId,
        String numeroSinistro,
        LocalDate dataOcorrido,
        String descricao,
        BigDecimal valorSolicitado,
        BigDecimal valorAprovado,
        StatusSinistro statusSinistro,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
