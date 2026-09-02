-- Trigger para atualizar a coluna de localização com base nas colunas de latitude e longitude
-- A trigger é acionada sempre que uma rectenna é inserida ou atualizada
CREATE OR REPLACE TRIGGER TRG_ANTENA_LOCALIZACAO
BEFORE INSERT OR UPDATE OF LATITUDE, LONGITUDE
ON AST_RECTENNA
FOR EACH ROW
BEGIN
    :NEW.LOCALIZACAO :=
        MDSYS.SDO_GEOMETRY(
            2001,
            4326,
            MDSYS.SDO_POINT_TYPE(
                :NEW.LONGITUDE,
                :NEW.LATITUDE,
                NULL
            ),
            NULL,
            NULL
        );
END;
/