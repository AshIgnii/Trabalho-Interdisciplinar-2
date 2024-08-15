USE interdisciplinar;
CREATE TABLE IF NOT EXISTS tb_pokemon_totalizador
(
    id         INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    tipo       VARCHAR(10)                    NOT NULL,
    quantidade INT                            NOT NULL
);