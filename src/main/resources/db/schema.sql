CREATE TABLE IF NOT EXISTS usuarios (
    id         UUID PRIMARY KEY,
    cpf        VARCHAR(11)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(72)  NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id UUID NOT NULL REFERENCES usuarios (id),
    role       VARCHAR(20) NOT NULL,
    PRIMARY KEY (usuario_id, role)
);
