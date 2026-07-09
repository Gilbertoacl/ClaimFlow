CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) UNIQUE NOT NULL,
    senhaHash VARCHAR(255) NOT NULL,
    funcao VARCHAR(50) NOT NULL,
    cliente_id UUID,
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);