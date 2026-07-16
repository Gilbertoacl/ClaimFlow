CREATE TABLE apolices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    tipo_apolice VARCHAR(20) NOT NULL,
    numero_apolice VARCHAR(20) NOT NULL UNIQUE,
    valor_segurado DECIMAL(10, 2) NOT NULL,
    premio_mensal DECIMAL(10, 2) NOT NULL,
    inicio_vigencia TIMESTAMPTZ NOT NULL,
    fim_vigencia TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_apolice_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT chk_apolice_tipo CHECK (tipo_apolice IN ('AUTOMOVEL', 'RESIDENCIAL', 'VIDA', 'SAUDE')),
    CONSTRAINT chk_apolice_status CHECK (status IN ('ATIVA', 'CANCELADA', 'VENCIDA')),
    CONSTRAINT chk_apolice_vigencia CHECK (fim_vigencia > inicio_vigencia)
);