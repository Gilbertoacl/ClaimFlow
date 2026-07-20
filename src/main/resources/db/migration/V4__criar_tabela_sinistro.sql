CREATE TABLE sinistros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apolice_id UUID NOT NULL ,
    numero_sinistro VARCHAR(15) UNIQUE NOT NULL,
    data_ocorrido DATE NOT NULL ,
    descricao VARCHAR(255),
    valor_solicitado DECIMAL(10, 2) NOT NULL,
    valor_aprovado DECIMAL(10, 2),
    status_sinistro VARCHAR(20) NOT NULL ,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_sinistro_apolice FOREIGN KEY (apolice_id) REFERENCES apolices(id),
    CONSTRAINT chk_sinistro_status CHECK (status_sinistro IN ('ABERTO', 'EM_ANALISE', 'APROVADO', 'NEGADO', 'PAGO'))
);

CREATE INDEX idx_sinistro_apolice ON sinistros(apolice_id);