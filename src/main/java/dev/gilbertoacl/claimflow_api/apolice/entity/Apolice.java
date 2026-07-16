package dev.gilbertoacl.claimflow_api.apolice.entity;

import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "apolices")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Apolice {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_apolice", length = 20, nullable = false)
    private TipoApolice tipoApolice;

    @Column(name = "numero_apolice", unique = true, nullable = false, length = 20)
    private String numeroApolice;

    @Column(name = "valor_segurado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSegurado;

    @Column(name = "premio_mensal", nullable = false, precision = 10, scale = 2)
    private BigDecimal premioMensal;

    @Column(name = "inicio_vigencia", nullable = false)
    private OffsetDateTime inicioVigencia;

    @Column(name = "fim_vigencia", nullable = false)
    private OffsetDateTime  fimVigencia;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusApolice status;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
}
