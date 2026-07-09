package dev.gilbertoacl.claimflow_api.cliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Endereco {
    @Column(length = 255, nullable = false)
    private String logradouro;

    @Column(length = 20, nullable = false)
    private String numero;

    @Column(length = 255)
    private String complemento;

    @Column(length = 35, nullable = false)
    private String bairro;

    @Column(length = 30, nullable = false)
    private String estado;

    @Column(length = 7, nullable = false)
    private String cep;
}
