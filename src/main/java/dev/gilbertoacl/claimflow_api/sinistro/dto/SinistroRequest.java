package dev.gilbertoacl.claimflow_api.sinistro.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SinistroRequest(
        @NotNull UUID apoliceId,
        @NotNull @PastOrPresent LocalDate dataOcorrido,
        @NotBlank @Size(max = 255) String decricao,
        @NotNull @Positive BigDecimal valorSolicitado
) {
}
