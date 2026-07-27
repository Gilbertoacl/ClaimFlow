package dev.gilbertoacl.claimflow_api.historicosinistro.entity;

import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_sinistros")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class HistoricoSinistro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sinsitro_id", nullable = false)
    private UUID sinistroId;

    @Column(name = "numero_sinsitro", nullable = false, length = 15)
    private String numeroSinistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_sinistro", nullable = false, length = 20)
    private StatusSinistro statusSinistro;

    @Column(name = "responsavel_id", nullable = false)
    private UUID responsavelId;

    @Column
    private String observacao;

    @CreationTimestamp
    @Column(name = "data_alteracao", nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;
}
