CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(255),
    bairro VARCHAR(35) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    cep VARCHAR(7) NOT NULL,
    data_nascimento DATE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT now()
);