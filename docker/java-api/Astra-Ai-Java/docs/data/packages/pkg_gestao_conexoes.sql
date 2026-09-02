-- Spec

CREATE OR REPLACE PACKAGE PKG_GESTAO_CONEXOES AS
    FUNCTION fn_rectenna_mais_proxima(id_rectenna_origem NUMBER) RETURN NUMBER;
    PROCEDURE VALIDAR_REDIRECIONAMENTO(P_ID_SATELITE IN NUMBER, P_ID_RECTENNA IN NUMBER);
END PKG_GESTAO_CONEXOES;
/

-- Body

CREATE OR REPLACE PACKAGE BODY PKG_GESTAO_CONEXOES AS

    FUNCTION fn_rectenna_mais_proxima(id_rectenna_origem NUMBER) RETURN NUMBER IS
        localizacao_rectenna_origem MDSYS.SDO_GEOMETRY;
        id_rectenna_destino NUMBER;
    BEGIN
        SELECT localizacao INTO localizacao_rectenna_origem
        FROM AST_RECTENNA WHERE id_rectenna = id_rectenna_origem;

        SELECT id_rectenna INTO id_rectenna_destino
        FROM AST_RECTENNA a
        WHERE a.id_rectenna <> id_rectenna_origem
          AND a.status_operacional = 'Ativa'
          AND SDO_NN(a.localizacao, localizacao_rectenna_origem, 'sdo_num_res=2', 1) = 'TRUE'
        FETCH FIRST 1 ROW ONLY;

        RETURN id_rectenna_destino;
    END fn_rectenna_mais_proxima;

    PROCEDURE VALIDAR_REDIRECIONAMENTO(P_ID_SATELITE IN NUMBER, P_ID_RECTENNA IN NUMBER) AS
        V_QTD_REDIRECIONAMENTO NUMBER;
        V_ID_RECTENNA_ORIGEM   NUMBER;
    BEGIN
        SELECT COUNT(*) INTO V_QTD_REDIRECIONAMENTO
        FROM AST_HISTORICO_CONEXAO
        WHERE ID_SATELITE = P_ID_SATELITE AND STATUS_CONEXAO = 'Transmitindo';

        IF V_QTD_REDIRECIONAMENTO > 0 THEN
            SELECT ID_RECTENNA INTO V_ID_RECTENNA_ORIGEM
            FROM AST_HISTORICO_CONEXAO
            WHERE ID_SATELITE = P_ID_SATELITE AND STATUS_CONEXAO = 'Transmitindo'
            FETCH FIRST 1 ROW ONLY;

            UPDATE AST_HISTORICO_CONEXAO
            SET STATUS_CONEXAO = 'Desviado por Clima'
            WHERE ID_SATELITE = P_ID_SATELITE AND STATUS_CONEXAO = 'Transmitindo';

            INSERT INTO AST_HISTORICO_DESVIO VALUES (
                SEQ_AST_DESVIO.NEXTVAL, P_ID_SATELITE, V_ID_RECTENNA_ORIGEM, P_ID_RECTENNA, SYSDATE
            );
        END IF;
    END VALIDAR_REDIRECIONAMENTO;

END PKG_GESTAO_CONEXOES;
/