package dev.gilbertoacl.claimflow_api.apolice.dto;

import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApoliceResponse(
        UUID id,
        UUID clienteId,
        TipoApolice tipoApolice,
        String numeroApolice,
        BigDecimal valorSegurado,
        BigDecimal premioMensal,
        OffsetDateTime inicioVigencia,
        OffsetDateTime fimVigencia,
        StatusApolice statusApolice,
        LocalDateTime dataCriacao
) {
}
