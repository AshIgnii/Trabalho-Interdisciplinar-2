USE interdisciplinar;

DELIMITER $$

CREATE PROCEDURE getValidationInfo()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM tb_pokemon_eletrico) AS quantidade_eletrico,
        (SELECT COUNT(*) FROM tb_pokemon_fogo) AS quantidade_fogo,
        (SELECT COUNT(*) FROM tb_pokemon_voador) AS quantidade_voador;

    SELECT id, pokemon, tipo
    FROM tb_pokemon;

    SELECT tb_pokemon.id, pokemon, tipo
    FROM tb_pokemon_eletrico
             JOIN tb_pokemon ON tb_pokemon_eletrico.id = tb_pokemon.id
    UNION ALL
    SELECT tb_pokemon.id, pokemon, tipo
    FROM tb_pokemon_fogo
             JOIN tb_pokemon ON tb_pokemon_fogo.id = tb_pokemon.id
    UNION ALL
    SELECT tb_pokemon.id, pokemon, tipo
    FROM tb_pokemon_voador
             JOIN tb_pokemon ON tb_pokemon_voador.id = tb_pokemon.id;

    SELECT id, pokemon, tipo
    FROM tb_pokemon_deletados;
END$$

DELIMITER ;