USE interdisciplinar;
CREATE TABLE IF NOT EXISTS tb_pokemon_voador
(
    id INT NOT NULL,

    CONSTRAINT tb_pokemon_voador_id_foreign_key FOREIGN KEY (id) REFERENCES tb_pokemon (id)
);