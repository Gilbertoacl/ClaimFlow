package dev.gilbertoacl.claimflow_api.cliente.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 255, nullable = false)
    private String nome;

    @Column(length = 11, unique = true)
    private String cpf;

    @Column(length = 255, unique = true)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Embedded
    private Endereco endereco;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;
}
