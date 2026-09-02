-- =============================================================
-- INIT - Astra IA | Oracle XE (Docker)
-- =============================================================
-- Cria todo o schema RM565421 dentro do PDB ASTRAIA:
--   - Tabelas da API Java (colunas MAIUSCULAS, padrao Hibernate)
--   - Tabelas da API .NET  (colunas "minusculas", padrao EF Core)
--   - Sequences para todas as tabelas
--   - Triggers de auto-increment (geram ID quando NULL)
--   - Foreign Keys entre todas as tabelas
--   - Seed inicial com 20 registros distribuidos
--
-- ATENCAO:
--   - Tabelas .NET usam colunas entre aspas duplas ("minusculas")
--     porque o EF Core gera queries case-sensitive.
--   - A coluna "id_rcdenna_origem" na AST_LEILAO_BIDDING tem um
--     typo intencional (deveria ser rectenna) que vem do codigo
--     .NET e NAO pode ser alterado.
--   - NAO usar GENERATED ALWAYS AS IDENTITY nas tabelas .NET
--     porque o EF Core envia NULL e o Oracle rejeita com ORA-01400.
--     Usamos triggers + sequences no lugar.
-- =============================================================

ALTER SESSION SET CONTAINER = ASTRAIA;
ALTER SESSION SET CURRENT_SCHEMA = RM565421;

-- =============================================================
-- 1. SEQUENCES (criadas ANTES das tabelas e triggers)
-- =============================================================

-- 1.1 Sequences da API Java
CREATE SEQUENCE SEQ_AST_SATELITE  START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_AST_RECTENNA  START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_AST_DESVIO    START WITH 1 INCREMENT BY 1 NOCACHE;

-- 1.2 Sequences da API .NET
CREATE SEQUENCE SEQ_AST_CLIENTE   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_AST_LEILAO    START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_AST_TRANSACAO START WITH 1 INCREMENT BY 1 NOCACHE;

-- =============================================================
-- 2. TABELAS BASE DA API JAVA (referenciadas por outras via FK)
--    Estilo Hibernate: colunas MAIUSCULAS sem aspas
-- =============================================================

CREATE TABLE AST_SATELITE (
    ID_SATELITE         NUMBER          NOT NULL,
    NOME_SATELITE       VARCHAR2(50)    NOT NULL,
    STATUS_OPERACIONAL  VARCHAR2(20)    NOT NULL,
    EFICIENCIA_PAINEIS  NUMBER(5,2),
    CAPACIDADE_MAX_GW   NUMBER(5,2),
    CONSTRAINT AST_SATELITE_PK PRIMARY KEY (ID_SATELITE),
    CONSTRAINT CK_SAT_STATUS CHECK
        (STATUS_OPERACIONAL IN ('Ativo', 'Manutencao', 'Inativo'))
);

CREATE TABLE AST_RECTENNA (
    ID_RECTENNA              NUMBER          NOT NULL,
    NOME_SUBESTACAO          VARCHAR2(50)    NOT NULL,
    LATITUDE                 NUMBER(10,7)    NOT NULL,
    LONGITUDE                NUMBER(10,7)    NOT NULL,
    CAPACIDADE_SUPORTADA_GWH NUMBER(5,2),
    STATUS_OPERACIONAL       VARCHAR2(20)    NOT NULL,
    CONSTRAINT AST_RECTENNA_PK PRIMARY KEY (ID_RECTENNA),
    CONSTRAINT CK_RECTENNA_STATUS CHECK
        (STATUS_OPERACIONAL IN ('Ativa', 'Inativa'))
);

-- =============================================================
-- 3. TABELAS DA API .NET (Astra Ai)
--    Estilo EF Core: colunas "entre aspas duplas" minusculas
--    SEM IDENTITY (trigger + sequence geram o ID)
-- =============================================================

CREATE TABLE AST_CLIENTE_PREMIUM (
    "id_cliente"               NUMBER          NOT NULL,
    "razao_social"             VARCHAR2(100)   NOT NULL,
    "cnpj"                     VARCHAR2(14)    NOT NULL,
    "demanda_contratada_gwh"   NUMBER(6,2),
    "status_cadastro"          VARCHAR2(20)    NOT NULL,
    CONSTRAINT AST_CLIENTE_PREMIUM_PK PRIMARY KEY ("id_cliente"),
    CONSTRAINT UK_CLIENTE_CNPJ UNIQUE ("cnpj"),
    CONSTRAINT CK_CLIENTE_STATUS CHECK
        ("status_cadastro" IN ('Ativo', 'Bloqueado'))
);

CREATE TABLE AST_LEILAO_BIDDING (
    "id_leilao"             NUMBER          NOT NULL,
    "id_satelite"           NUMBER          NOT NULL,
    "id_rcdenna_origem"     NUMBER          NOT NULL,
    "data_hora_inicio"      TIMESTAMP       NOT NULL,
    "data_hora_fim"         TIMESTAMP       NOT NULL,
    "gwh_disponivel"        NUMBER(5,2),
    "preco_min_por_gwh"     NUMBER(10,2),
    "status_leilao"         VARCHAR2(20)    NOT NULL,
    CONSTRAINT AST_LEILAO_BIDDING_PK PRIMARY KEY ("id_leilao"),
    CONSTRAINT CK_LEILAO_STATUS CHECK
        ("status_leilao" IN ('Aberto', 'Finalizado', 'Cancelado'))
);

CREATE TABLE AST_LOG_TRANSACAO (
    "id_transacao"            NUMBER          NOT NULL,
    "id_leilao"               NUMBER          NOT NULL,
    "id_cliente_vencedor"     NUMBER          NOT NULL,
    "valor_arrematado"        NUMBER(12,2),
    "taxa_orquestracao_astra" NUMBER(10,2),
    "data_faturamento"        DATE            NOT NULL,
    CONSTRAINT AST_LOG_TRANSACAO_PK PRIMARY KEY ("id_transacao")
);

-- =============================================================
-- 4. TABELAS DA API JAVA (Spring/Hibernate)
--    Estilo Hibernate: colunas MAIUSCULAS sem aspas
-- =============================================================

CREATE TABLE AST_HISTORICO_CONEXAO (
    ID_SATELITE             NUMBER          NOT NULL,
    ID_RECTENNA             NUMBER          NOT NULL,
    DATA_HORA_CONEXAO       TIMESTAMP       NOT NULL,
    EFICIENCIA_TRANSMISSAO  NUMBER(5,2),
    STATUS_CONEXAO          VARCHAR2(30),
    CONSTRAINT PK_HIST_CONEXAO PRIMARY KEY (ID_SATELITE, ID_RECTENNA, DATA_HORA_CONEXAO),
    CONSTRAINT CK_HISTCON_STATUS CHECK
        (STATUS_CONEXAO IN ('Transmitindo', 'Desviado por Clima', 'Falha de Alinhamento'))
);

CREATE TABLE AST_HISTORICO_DESVIO (
    ID_DESVIO            NUMBER          NOT NULL,
    ID_SATELITE          NUMBER          NOT NULL,
    ID_RECTENNA_ORIGEM   NUMBER          NOT NULL,
    ID_RECTENNA_DESTINO  NUMBER          NOT NULL,
    DATA_HORA_MANOBRA    TIMESTAMP       NOT NULL,
    TEMPO_RESPOSTA_MS    NUMBER,
    CONSTRAINT AST_HISTORICO_DESVIO_PK PRIMARY KEY (ID_DESVIO)
);

-- =============================================================
-- 5. FOREIGN KEYS
-- =============================================================

-- FKs do HISTORICO_CONEXAO (Java -> Java)
ALTER TABLE AST_HISTORICO_CONEXAO ADD CONSTRAINT FK_HISTCON_SAT
    FOREIGN KEY (ID_SATELITE) REFERENCES AST_SATELITE (ID_SATELITE);

ALTER TABLE AST_HISTORICO_CONEXAO ADD CONSTRAINT FK_HISTCON_RECT
    FOREIGN KEY (ID_RECTENNA) REFERENCES AST_RECTENNA (ID_RECTENNA);

-- FKs do HISTORICO_DESVIO (Java -> Java)
ALTER TABLE AST_HISTORICO_DESVIO ADD CONSTRAINT FK_DESVIO_SAT
    FOREIGN KEY (ID_SATELITE) REFERENCES AST_SATELITE (ID_SATELITE);

ALTER TABLE AST_HISTORICO_DESVIO ADD CONSTRAINT FK_DESVIO_ORIGEM
    FOREIGN KEY (ID_RECTENNA_ORIGEM) REFERENCES AST_RECTENNA (ID_RECTENNA);

ALTER TABLE AST_HISTORICO_DESVIO ADD CONSTRAINT FK_DESVIO_DESTINO
    FOREIGN KEY (ID_RECTENNA_DESTINO) REFERENCES AST_RECTENNA (ID_RECTENNA);

-- FKs do LEILAO_BIDDING (.NET -> Java)
ALTER TABLE AST_LEILAO_BIDDING ADD CONSTRAINT FK_LEILAO_SAT
    FOREIGN KEY ("id_satelite") REFERENCES AST_SATELITE (ID_SATELITE);

-- NOTA: FK removida de "id_rcdenna_origem" -> AST_RECTENNA porque
-- o nome da coluna .NET tem typo ("rcdenna" vs "rectenna").
-- A integridade desse campo fica a cargo da aplicacao .NET.

-- FKs do LOG_TRANSACAO (.NET -> .NET)
ALTER TABLE AST_LOG_TRANSACAO ADD CONSTRAINT FK_TRANS_LEILAO
    FOREIGN KEY ("id_leilao") REFERENCES AST_LEILAO_BIDDING ("id_leilao");

ALTER TABLE AST_LOG_TRANSACAO ADD CONSTRAINT FK_TRANS_CLIENTE
    FOREIGN KEY ("id_cliente_vencedor") REFERENCES AST_CLIENTE_PREMIUM ("id_cliente");

-- =============================================================
-- 6. TRIGGERS DE AUTO-INCREMENT
--    Geram o ID via sequence quando a aplicacao envia NULL
-- =============================================================

-- 6.1 Triggers da API Java
CREATE OR REPLACE TRIGGER TRG_AST_SATELITE
BEFORE INSERT ON AST_SATELITE
FOR EACH ROW
WHEN (NEW.ID_SATELITE IS NULL)
BEGIN
    :NEW.ID_SATELITE := SEQ_AST_SATELITE.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER TRG_AST_RECTENNA
BEFORE INSERT ON AST_RECTENNA
FOR EACH ROW
WHEN (NEW.ID_RECTENNA IS NULL)
BEGIN
    :NEW.ID_RECTENNA := SEQ_AST_RECTENNA.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER TRG_AST_DESVIO
BEFORE INSERT ON AST_HISTORICO_DESVIO
FOR EACH ROW
WHEN (NEW.ID_DESVIO IS NULL)
BEGIN
    :NEW.ID_DESVIO := SEQ_AST_DESVIO.NEXTVAL;
END;
/

-- 6.2 Triggers da API .NET (colunas entre aspas = case-sensitive)
CREATE OR REPLACE TRIGGER TRG_AST_CLIENTE
BEFORE INSERT ON AST_CLIENTE_PREMIUM
FOR EACH ROW
WHEN (NEW."id_cliente" IS NULL)
BEGIN
    :NEW."id_cliente" := SEQ_AST_CLIENTE.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER TRG_AST_LEILAO
BEFORE INSERT ON AST_LEILAO_BIDDING
FOR EACH ROW
WHEN (NEW."id_leilao" IS NULL)
BEGIN
    :NEW."id_leilao" := SEQ_AST_LEILAO.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER TRG_AST_TRANSACAO
BEFORE INSERT ON AST_LOG_TRANSACAO
FOR EACH ROW
WHEN (NEW."id_transacao" IS NULL)
BEGIN
    :NEW."id_transacao" := SEQ_AST_TRANSACAO.NEXTVAL;
END;
/

-- =============================================================
-- 7. SEED INICIAL (20 registros distribuidos)
--    Ordem respeita as FKs: tabelas-base primeiro, dependentes depois.
--    IDs ficam NULL para os triggers gerarem (proximos da sequence).
-- =============================================================

-- 7.1 AST_SATELITE (3 registros) - IDs gerados: 1, 2, 3
INSERT INTO AST_SATELITE (NOME_SATELITE, STATUS_OPERACIONAL, EFICIENCIA_PAINEIS, CAPACIDADE_MAX_GW)
VALUES ('ASTRA-SOL-01', 'Ativo',      94.50, 2.80);

INSERT INTO AST_SATELITE (NOME_SATELITE, STATUS_OPERACIONAL, EFICIENCIA_PAINEIS, CAPACIDADE_MAX_GW)
VALUES ('ASTRA-SOL-02', 'Manutencao', 88.20, 3.10);

INSERT INTO AST_SATELITE (NOME_SATELITE, STATUS_OPERACIONAL, EFICIENCIA_PAINEIS, CAPACIDADE_MAX_GW)
VALUES ('ASTRA-SOL-03', 'Ativo',      96.80, 4.20);

-- 7.2 AST_RECTENNA (3 registros) - IDs gerados: 1, 2, 3
INSERT INTO AST_RECTENNA (NOME_SUBESTACAO, LATITUDE, LONGITUDE, CAPACIDADE_SUPORTADA_GWH, STATUS_OPERACIONAL)
VALUES ('Subestacao Nordeste BR',  -3.7319, -38.5267, 5.00, 'Ativa');

INSERT INTO AST_RECTENNA (NOME_SUBESTACAO, LATITUDE, LONGITUDE, CAPACIDADE_SUPORTADA_GWH, STATUS_OPERACIONAL)
VALUES ('Subestacao Sudeste BR',  -23.5505, -46.6333, 8.50, 'Ativa');

INSERT INTO AST_RECTENNA (NOME_SUBESTACAO, LATITUDE, LONGITUDE, CAPACIDADE_SUPORTADA_GWH, STATUS_OPERACIONAL)
VALUES ('Subestacao Sul BR',      -30.0346, -51.2177, 7.80, 'Ativa');

-- 7.3 AST_CLIENTE_PREMIUM (3 registros) - IDs gerados: 1, 2, 3
INSERT INTO AST_CLIENTE_PREMIUM ("razao_social", "cnpj", "demanda_contratada_gwh", "status_cadastro")
VALUES ('Siderurgica Acos do Brasil SA', '11222333000181', 4.50, 'Ativo');

INSERT INTO AST_CLIENTE_PREMIUM ("razao_social", "cnpj", "demanda_contratada_gwh", "status_cadastro")
VALUES ('Petroquimica Nordeste Ltda',    '22333444000192', 3.20, 'Ativo');

INSERT INTO AST_CLIENTE_PREMIUM ("razao_social", "cnpj", "demanda_contratada_gwh", "status_cadastro")
VALUES ('Mineracao Vale Verde SA',       '33444555000103', 6.80, 'Ativo');

-- 7.4 AST_LEILAO_BIDDING (3 registros) - IDs gerados: 1, 2, 3
INSERT INTO AST_LEILAO_BIDDING ("id_satelite", "id_rcdenna_origem", "data_hora_inicio", "data_hora_fim", "gwh_disponivel", "preco_min_por_gwh", "status_leilao")
VALUES (1, 1, TIMESTAMP '2025-05-01 00:00:00', TIMESTAMP '2025-05-01 23:59:59', 2.50, 150000.00, 'Finalizado');

INSERT INTO AST_LEILAO_BIDDING ("id_satelite", "id_rcdenna_origem", "data_hora_inicio", "data_hora_fim", "gwh_disponivel", "preco_min_por_gwh", "status_leilao")
VALUES (3, 2, TIMESTAMP '2025-05-02 00:00:00', TIMESTAMP '2025-05-02 23:59:59', 4.00, 180000.00, 'Finalizado');

INSERT INTO AST_LEILAO_BIDDING ("id_satelite", "id_rcdenna_origem", "data_hora_inicio", "data_hora_fim", "gwh_disponivel", "preco_min_por_gwh", "status_leilao")
VALUES (1, 3, TIMESTAMP '2025-05-25 00:00:00', TIMESTAMP '2025-05-25 23:59:59', 4.10, 185000.00, 'Aberto');

-- 7.5 AST_HISTORICO_CONEXAO (3 registros) - PK composta, sem trigger
INSERT INTO AST_HISTORICO_CONEXAO (ID_SATELITE, ID_RECTENNA, DATA_HORA_CONEXAO, EFICIENCIA_TRANSMISSAO, STATUS_CONEXAO)
VALUES (1, 1, TIMESTAMP '2025-05-01 06:00:00', 97.30, 'Transmitindo');

INSERT INTO AST_HISTORICO_CONEXAO (ID_SATELITE, ID_RECTENNA, DATA_HORA_CONEXAO, EFICIENCIA_TRANSMISSAO, STATUS_CONEXAO)
VALUES (1, 2, TIMESTAMP '2025-05-01 08:30:00', 85.10, 'Desviado por Clima');

INSERT INTO AST_HISTORICO_CONEXAO (ID_SATELITE, ID_RECTENNA, DATA_HORA_CONEXAO, EFICIENCIA_TRANSMISSAO, STATUS_CONEXAO)
VALUES (3, 3, TIMESTAMP '2025-05-02 06:00:00', 99.10, 'Transmitindo');

-- 7.6 AST_HISTORICO_DESVIO (2 registros) - IDs gerados: 1, 2
INSERT INTO AST_HISTORICO_DESVIO (ID_SATELITE, ID_RECTENNA_ORIGEM, ID_RECTENNA_DESTINO, DATA_HORA_MANOBRA, TEMPO_RESPOSTA_MS)
VALUES (1, 1, 2, TIMESTAMP '2025-05-01 08:28:00', 120);

INSERT INTO AST_HISTORICO_DESVIO (ID_SATELITE, ID_RECTENNA_ORIGEM, ID_RECTENNA_DESTINO, DATA_HORA_MANOBRA, TEMPO_RESPOSTA_MS)
VALUES (3, 2, 3, TIMESTAMP '2025-05-02 10:15:00',  95);

-- 7.7 AST_LOG_TRANSACAO (3 registros) - IDs gerados: 1, 2, 3
INSERT INTO AST_LOG_TRANSACAO ("id_leilao", "id_cliente_vencedor", "valor_arrematado", "taxa_orquestracao_astra", "data_faturamento")
VALUES (1, 1, 412500.00, 20625.00, DATE '2025-05-02');

INSERT INTO AST_LOG_TRANSACAO ("id_leilao", "id_cliente_vencedor", "valor_arrematado", "taxa_orquestracao_astra", "data_faturamento")
VALUES (1, 2, 380000.00, 19000.00, DATE '2025-05-02');

INSERT INTO AST_LOG_TRANSACAO ("id_leilao", "id_cliente_vencedor", "valor_arrematado", "taxa_orquestracao_astra", "data_faturamento")
VALUES (2, 3, 756000.00, 37800.00, DATE '2025-05-03');

COMMIT;
