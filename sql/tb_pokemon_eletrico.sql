USE interdisciplinar;
CREATE TABLE IF NOT EXISTS tb_pokemon_eletrico
(
    id INT NOT NULL,

    CONSTRAINT tb_pokemon_id_foreign_key FOREIGN KEY (id) REFERENCES tb_pokemon (id)
);