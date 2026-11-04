-- Finance Balance: schema inicial (modulos P1)
-- Valores monetarios sempre NUMERIC(15,2); todo registro de usuario possui usuario_id.

CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(120)  NOT NULL,
    email         VARCHAR(160)  NOT NULL,
    senha_hash    VARCHAR(100)  NOT NULL,
    renda_mensal  NUMERIC(15,2),
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_renda CHECK (renda_mensal IS NULL OR renda_mensal >= 0)
);

CREATE TABLE contas (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome           VARCHAR(80)   NOT NULL,
    tipo           VARCHAR(20)   NOT NULL,
    saldo_inicial  NUMERIC(15,2) NOT NULL DEFAULT 0,
    ativa          BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_contas_usuario_nome UNIQUE (usuario_id, nome)
);

-- usuario_id NULL = categoria padrao do sistema (somente leitura)
CREATE TABLE categorias (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT      REFERENCES usuarios(id) ON DELETE CASCADE,
    categoria_pai_id BIGINT    REFERENCES categorias(id),
    nome           VARCHAR(80) NOT NULL,
    tipo           VARCHAR(10) NOT NULL,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_categorias_usuario ON categorias(usuario_id);
CREATE INDEX ix_categorias_pai ON categorias(categoria_pai_id);

CREATE TABLE formas_pagamento (
    id    BIGSERIAL PRIMARY KEY,
    nome  VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE etiquetas (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome        VARCHAR(40) NOT NULL,
    CONSTRAINT uk_etiquetas_usuario_nome UNIQUE (usuario_id, nome)
);

CREATE TABLE transacoes (
    id                BIGSERIAL PRIMARY KEY,
    usuario_id        BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    descricao         VARCHAR(200)  NOT NULL,
    valor             NUMERIC(15,2) NOT NULL,
    tipo              VARCHAR(15)   NOT NULL,
    situacao          VARCHAR(10)   NOT NULL DEFAULT 'PAGA',
    data              DATE          NOT NULL,
    data_vencimento   DATE,
    observacao        VARCHAR(500),
    categoria_id      BIGINT        REFERENCES categorias(id),
    conta_id          BIGINT        NOT NULL REFERENCES contas(id),
    conta_destino_id  BIGINT        REFERENCES contas(id),
    forma_pagamento_id BIGINT       REFERENCES formas_pagamento(id),
    criado_em         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_transacoes_valor CHECK (valor > 0),
    CONSTRAINT ck_transacoes_transferencia CHECK (
        (tipo = 'TRANSFERENCIA' AND conta_destino_id IS NOT NULL AND conta_destino_id <> conta_id AND categoria_id IS NULL)
        OR (tipo <> 'TRANSFERENCIA' AND conta_destino_id IS NULL AND categoria_id IS NOT NULL)
    )
);
CREATE INDEX ix_transacoes_usuario_data ON transacoes(usuario_id, data DESC);
CREATE INDEX ix_transacoes_categoria ON transacoes(categoria_id);
CREATE INDEX ix_transacoes_conta ON transacoes(conta_id);
CREATE INDEX ix_transacoes_conta_destino ON transacoes(conta_destino_id);

CREATE TABLE transacao_etiquetas (
    transacao_id BIGINT NOT NULL REFERENCES transacoes(id) ON DELETE CASCADE,
    etiqueta_id  BIGINT NOT NULL REFERENCES etiquetas(id) ON DELETE CASCADE,
    PRIMARY KEY (transacao_id, etiqueta_id)
);

-- mes sempre no primeiro dia do mes
CREATE TABLE orcamentos (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    categoria_id  BIGINT        NOT NULL REFERENCES categorias(id),
    mes           DATE          NOT NULL,
    valor_limite  NUMERIC(15,2) NOT NULL,
    CONSTRAINT uk_orcamentos UNIQUE (usuario_id, categoria_id, mes),
    CONSTRAINT ck_orcamentos_limite CHECK (valor_limite > 0),
    CONSTRAINT ck_orcamentos_mes CHECK (EXTRACT(DAY FROM mes) = 1)
);

CREATE TABLE regras_distribuicao (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    categoria_id  BIGINT       NOT NULL REFERENCES categorias(id),
    percentual    NUMERIC(5,2) NOT NULL,
    CONSTRAINT uk_regras_distribuicao UNIQUE (usuario_id, categoria_id),
    CONSTRAINT ck_regras_percentual CHECK (percentual > 0 AND percentual <= 100)
);

CREATE TABLE investimentos (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome            VARCHAR(100)  NOT NULL,
    tipo            VARCHAR(30)   NOT NULL,
    valor_aplicado  NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_atual     NUMERIC(15,2) NOT NULL DEFAULT 0,
    data_aplicacao  DATE          NOT NULL,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_investimentos_valores CHECK (valor_aplicado >= 0 AND valor_atual >= 0)
);
CREATE INDEX ix_investimentos_usuario ON investimentos(usuario_id);

CREATE TABLE movimentos_investimento (
    id               BIGSERIAL PRIMARY KEY,
    investimento_id  BIGINT        NOT NULL REFERENCES investimentos(id) ON DELETE CASCADE,
    tipo             VARCHAR(15)   NOT NULL,
    valor            NUMERIC(15,2) NOT NULL,
    data             DATE          NOT NULL,
    CONSTRAINT ck_movimentos_valor CHECK (valor > 0)
);
CREATE INDEX ix_movimentos_investimento ON movimentos_investimento(investimento_id, data DESC);
