package dev.gilbertoacl.claimflow_api.sinistro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sinistros")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Sinistro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "apolice_id", nullable = false)
    private UUID apoliceId;

    @Column(name = "numero_sinistro", nullable = false, unique = true, length = 15)
    private String numeroSinistro;

    @Column(name = "data_ocorrido", nullable = false)
    private LocalDate dataOcorrido;

    @Column
    private String descricao;

    @Column(name = "valor_solicitado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(name = "valor_aprovado", precision = 10, scale = 2)
    private BigDecimal valorAprovado;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_sinistro", nullable = false, length = 20)
    private StatusSinistro statusSinistro;

    @CreationTimestamp
    @Column(name = "data_criacao",nullable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;
}
