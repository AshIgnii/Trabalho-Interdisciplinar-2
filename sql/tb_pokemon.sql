USE interdisciplinar;
CREATE TABLE IF NOT EXISTS tb_pokemon
(
    id      INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    pokemon VARCHAR(50)                    NOT NULL,
    tipo    VARCHAR(10)                    NOT NULL
);