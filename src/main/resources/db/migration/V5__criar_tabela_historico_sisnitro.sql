CREATE TABLE historico_sinistros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sinistro_id UUID NOT NULL,
    numero_sinistro VARCHAR(15) NOT NULL,
    status_sinistro VARCHAR(20) NOT NULL,
    responsavel_id UUID NOT NULL,
    observacao VARCHAR(255),
    data_alteracao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_historico_sinistro FOREIGN KEY (sinistro_id) REFERENCES sinistros(id),
    CONSTRAINT fk_historico_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuarios(id),
    CONSTRAINT chk_historico_status CHECK (status_sinistro IN ('ABERTO', 'EM_ANALISE', 'APROVADO', 'NEGADO', 'PAGO'))
);

CREATE INDEX idx_historico_sinistro ON historico_sinistros(sinistro_id);