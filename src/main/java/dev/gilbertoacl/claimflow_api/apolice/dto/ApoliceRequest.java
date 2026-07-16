package dev.gilbertoacl.claimflow_api.apolice.dto;

import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApoliceRequest(
        @NotNull UUID clienteId,
        @NotNull TipoApolice tipoApolice,
        @NotBlank @Size(max = 20) String numeroApolice,
        @NotNull @Positive BigDecimal valorSegurado,
        @NotNull @Positive BigDecimal premioMensal,
        @NotNull OffsetDateTime inicioVigencia,
        @NotNull OffsetDateTime fimVigencia
) {
}
