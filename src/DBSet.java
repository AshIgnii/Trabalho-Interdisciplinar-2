import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DBSet {
    private final Connection connection;
    private final DBGet dbGet;
    private final List<String> typeTables = Arrays.asList("tb_pokemon_fogo", "tb_pokemon_voador", "tb_pokemon_eletrico");
    
    public DBSet(Connection con, DBGet dbGet) {
        this.connection = con;
        this.dbGet = dbGet;
    }

    public void insertPokemon(Pokemon pokemon) {
        try {
            boolean dp = dbGet.duplicateInMainTable(pokemon.getNome(), pokemon.getTipo());
            if (!dp) {
                assert connection != null;
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO tb_pokemon (pokemon, tipo) VALUES (?, ?)");
                insertStatement.setString(1, pokemon.getNome());
                insertStatement.setString(2, pokemon.getTipo());
                insertStatement.executeUpdate();
            } else {
                System.out.println("Pokemon já existe na tabela principal e não foi inserido.");
            }
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }

        refreshTypeTables();
        updateCountTable();
    }

    public void insertPokemon(List<Pokemon> pokemons) {
        try {
            List<Pokemon> currentPokemons = dbGet.getPokemons();
            List<Integer> duplicateIndexes = new ArrayList<>();
            for (Pokemon poke : pokemons) {
                for (Pokemon cPoke : currentPokemons) {
                    if (Utils.equalsInsensitive(poke.getNome(), cPoke.getNome()) && Utils.equalsInsensitive(poke.getTipo(), cPoke.getTipo())) {
                        int index = pokemons.indexOf(poke);
                        if (!duplicateIndexes.contains(index)) {
                            duplicateIndexes.add(index);
                            break;
                        }
                    }
                }
            }

            if (duplicateIndexes.size() > 0) {
                System.out.println("Alguns pokemons já existem na tabela principal e não foram inseridos.");

                for (int i = duplicateIndexes.size() - 1; i >= 0; i--) {
                    pokemons.remove((int) duplicateIndexes.get(i));
                }
            }

            if (pokemons.isEmpty()) {
                refreshTypeTables();
                return;
            }

            StringBuilder values = new StringBuilder();
            for (Pokemon pokemon : pokemons) {
                values.append("('").append(pokemon.getNome()).append("', '").append(pokemon.getTipo()).append("'),");
            }
            values.deleteCharAt(values.length() - 1);

            assert connection != null;
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO tb_pokemon (pokemon, tipo) VALUES " + values);
            insertStatement.executeUpdate();

            refreshTypeTables();
            updateCountTable();
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }
    }

    public void insertTypeTable(String tipo, Set<Integer> ids) {
        try {
            if (ids.isEmpty()) {
                return;
            }

            assert connection != null;

            StringBuilder values = new StringBuilder();
            for (int id : ids) {
                values.append("(").append(id).append("),");
            }
            values.deleteCharAt(values.length() - 1);

            tipo = tipo.toLowerCase(Locale.ROOT);
            tipo = "tb_pokemon_" + tipo;

            if (!typeTables.contains(tipo)) {
                throw new IllegalArgumentException("Invalid table name: " + tipo);
            }

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + tipo + "(id) VALUES " + values);
            insertStatement.executeUpdate();
        } catch (SQLException | AssertionError | IllegalArgumentException e) {
            ErrorHandler.handleError(e);
        }

        updateCountTable();
    }

    public void deletePokemon(List<Integer> ids) {
        if (ids.isEmpty()) {
            return;
        }

        try {
            assert connection != null;

            List<Pokemon> deletedPokemons = dbGet.getPokemons(ids);
            StringBuilder deletedValues = new StringBuilder();
            for (Pokemon poke : deletedPokemons) {
                deletedValues.append("(").append(poke.getId()).append(", '").append(poke.getNome()).append("', '").append(poke.getTipo()).append("'),");
            }
            deletedValues.deleteCharAt(deletedValues.length() - 1);

            PreparedStatement insertDeleted = connection.prepareStatement("INSERT INTO tb_pokemon_deletados (id, pokemon, tipo) VALUES " + deletedValues);
            insertDeleted.executeUpdate();

            StringBuilder values = new StringBuilder();
            for (int id : ids) {
                values.append(id).append(",");
            }
            values.deleteCharAt(values.length() - 1);

            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM tb_pokemon WHERE id IN (" + values + ")");
            deleteStatement.executeUpdate();
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }
    }

    public void purgeTables() {
        try {
            assert connection != null;

            String[] sqlStatements = {
                    "DELETE FROM tb_pokemon_totalizador WHERE TRUE;",
                    "DELETE FROM tb_pokemon_fogo WHERE TRUE;",
                    "DELETE FROM tb_pokemon_eletrico WHERE TRUE;",
                    "DELETE FROM tb_pokemon_voador WHERE TRUE;",
                    "DELETE FROM tb_pokemon_deletados WHERE TRUE;",
                    "DELETE FROM tb_pokemon WHERE TRUE;",
                    "ALTER TABLE tb_pokemon AUTO_INCREMENT = 1;",
                    "ALTER TABLE tb_pokemon_totalizador AUTO_INCREMENT = 1;"
            };

            Statement statement = connection.createStatement();
            for (String sql : sqlStatements) {
                statement.execute(sql);
            }

            System.out.println("DB Resetada.");
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }
    }

    public void refreshTypeTables() {
        List<Pokemon> pokemons = dbGet.getPokemons();
        Set<String> names = new HashSet<>();
        Set<Integer> fireIDs = new HashSet<>();
        Set<Integer> flyIDs = new HashSet<>();
        Set<Integer> electricIDs = new HashSet<>();
        for (Pokemon pokemon : pokemons) {
            if (names.contains(pokemon.getNome())) {
                continue;
            }
            if (Utils.equalsInsensitive(pokemon.getTipo(), "fogo")) {
                names.add(pokemon.getNome());
                fireIDs.add(pokemon.getId());
            } else if (Utils.equalsInsensitive(pokemon.getTipo(), "voador")) {
                names.add(pokemon.getNome());
                flyIDs.add(pokemon.getId());
            } else if (Utils.equalsInsensitive(pokemon.getTipo(), "eletrico")) {
                names.add(pokemon.getNome());
                electricIDs.add(pokemon.getId());
            }
        }

        Set<Integer> fireTable = dbGet.getTypeTable("fogo");
        Set<Integer> flyTable = dbGet.getTypeTable("voador");
        Set<Integer> electricTable = dbGet.getTypeTable("eletrico");

        fireIDs.removeAll(fireTable);
        flyIDs.removeAll(flyTable);
        electricIDs.removeAll(electricTable);

        insertTypeTable("fogo", fireIDs);
        insertTypeTable("voador", flyIDs);
        insertTypeTable("eletrico", electricIDs);

        updateCountTable();
    }

    public void updateCountTable() {
        int duplicates = dbGet.getDuplicatesInMainTable().size();
        int fire = dbGet.getTypeTable("fogo").size();
        int fly = dbGet.getTypeTable("voador").size();
        int electric = dbGet.getTypeTable("eletrico").size();

        try {
            assert connection != null;

            String updateQuery = "UPDATE tb_pokemon_totalizador SET quantidade = CASE tipo WHEN 'duplicados' THEN ? WHEN 'fogo' THEN ? WHEN 'voador' THEN ? WHEN 'eletrico' THEN ? ELSE quantidade END";

            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setInt(1, duplicates);
            updateStatement.setInt(2, fire);
            updateStatement.setInt(3, fly);
            updateStatement.setInt(4, electric);

            updateStatement.executeUpdate();
        } catch (SQLException e) {
            ErrorHandler.handleError(e);
        }
    }

    public void deleteDuplicatesInMainTable() {
        List<Integer> duplicateIds = dbGet.getDuplicatesInMainTable();
        deletePokemon(duplicateIds);

        updateCountTable();
    }
}
