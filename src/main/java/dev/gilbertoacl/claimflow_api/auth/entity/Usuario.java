package dev.gilbertoacl.claimflow_api.auth.entity;

import dev.gilbertoacl.claimflow_api.auth.enums.Funcao;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Funcao funcao;

    @Column
    private UUID clienteId;
}
