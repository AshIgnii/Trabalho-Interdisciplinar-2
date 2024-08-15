import java.sql.*;
import java.util.*;

public class DBGet {
    private final Connection connection;
    private final List<String> typeTables = Arrays.asList("tb_pokemon_fogo", "tb_pokemon_voador", "tb_pokemon_eletrico");

    public DBGet(Connection con) {
        this.connection = con;
    }

    public List<Pokemon> getPokemons() {
        List<Pokemon> pokemons = new ArrayList<>();
        try {
            assert connection != null;
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tb_pokemon");
            while (rs.next()) {
                String nome = Utils.normalizeAcento(rs.getString("pokemon")).toLowerCase();
                String tipoPoke = Utils.normalizeAcento(rs.getString("tipo")).toLowerCase();
                pokemons.add(new Pokemon(rs.getInt("id"), nome, tipoPoke));
            }
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }

        return pokemons;
    }

    public List<Pokemon> getPokemons(List<Integer> ids) {
        List<Pokemon> pokemons = new ArrayList<>();
        try {
            assert connection != null;

            StringBuilder values = new StringBuilder();
            for (int id : ids) {
                values.append(id).append(",");
            }
            values.deleteCharAt(values.length() - 1);

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM tb_pokemon WHERE id IN (" + values + ")");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String nome = Utils.normalizeAcento(rs.getString("pokemon")).toLowerCase();
                String tipoPoke = Utils.normalizeAcento(rs.getString("tipo")).toLowerCase();
                pokemons.add(new Pokemon(rs.getInt("id"), nome, tipoPoke));
            }
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }

        return pokemons;
    }

    public List<Pokemon> getPokemons(String tipo) {
        tipo = Utils.normalizeAcento(tipo).toLowerCase();
        tipo = "tb_pokemon_" + tipo;

        List<Pokemon> pokemons = new ArrayList<>();
        try {
            if (!typeTables.contains(tipo)) {
                throw new IllegalArgumentException("Invalid table name: " + tipo);
            }

            assert connection != null;
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tipo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement getStatement = connection.prepareStatement("SELECT * FROM tb_pokemon WHERE id = ?");
                getStatement.setInt(1, rs.getInt("id"));
                ResultSet pokemon = getStatement.executeQuery();
                if (pokemon.next()) {
                    String nome = Utils.normalizeAcento(pokemon.getString("pokemon")).toLowerCase();
                    String tipoPoke = Utils.normalizeAcento(pokemon.getString("tipo")).toLowerCase();
                    pokemons.add(new Pokemon(pokemon.getInt("id"), nome, tipoPoke));
                }
            }
        } catch (SQLException | AssertionError | IllegalArgumentException e) {
            ErrorHandler.handleError(e);
        }

        return pokemons;
    }

    public Set<Integer> getTypeTable(String tipo) {
        tipo = tipo.toLowerCase(Locale.ROOT);
        tipo = "tb_pokemon_" + tipo;

        Set<Integer> ids = new HashSet<>();
        try {
            if (!typeTables.contains(tipo)) {
                throw new IllegalArgumentException("Invalid table name: " + tipo);
            }

            assert connection != null;
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tipo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException | AssertionError | IllegalArgumentException e) {
            ErrorHandler.handleError(e);
        }

        return ids;
    }

    public List<Quantifier> getCountTable() {
        List<Quantifier> quantificadores = new ArrayList<>();
        try {
            assert connection != null;
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tb_pokemon_totalizador");
            while (rs.next()) {
                quantificadores.add(new Quantifier(rs.getInt("id"), rs.getString("tipo"), rs.getInt("quantidade")));
            }
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }

        return quantificadores;
    }

    public List<Integer> getDuplicatesInMainTable() {
        List<Pokemon> pokemons = getPokemons();
        List<Integer> duplicates = new ArrayList<>();
        for (int i = 0; i < pokemons.size(); i++) {
            Pokemon pokemon = pokemons.get(i);
            for (int j = i + 1; j < pokemons.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (duplicates.contains(pokemon.getId())) {
                    break;
                }

                Pokemon poke = pokemons.get(j);
                if (Utils.equalsInsensitive(pokemon.getNome(), poke.getNome()) && Utils.equalsInsensitive(pokemon.getTipo(), poke.getTipo())) {
                    duplicates.add(poke.getId());
                    break;
                }
            }
        }

        return duplicates;
    }

    public boolean duplicateInMainTable(String nome, String tipo) {
        try {
            assert connection != null;
            PreparedStatement getStatement = connection.prepareStatement("SELECT * FROM tb_pokemon WHERE pokemon like ? collate utf8mb4_general_ci and tipo like ? collate utf8mb4_general_ci");
            getStatement.setString(1, nome);
            getStatement.setString(2, tipo);
            ResultSet rs = getStatement.executeQuery();
            return rs.next();
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }
        return false;
    }

    public boolean duplicatesInTypeTable(String nome, String tipo) {
        try {
            assert connection != null;
            PreparedStatement mainTableStatement = connection.prepareStatement("SELECT * FROM tb_pokemon WHERE pokemon like ? collate utf8mb4_general_ci and tipo like ? collate utf8mb4_general_ci");
            mainTableStatement.setString(1, nome);
            mainTableStatement.setString(2, tipo);
            ResultSet rs = mainTableStatement.executeQuery();
            if (rs.next()) {
                tipo = tipo.toLowerCase(Locale.ROOT);
                tipo = "tb_pokemon_" + tipo;

                int id = rs.getInt("id");
                PreparedStatement getStatement = connection.prepareStatement("SELECT * FROM ? WHERE id = ?");
                getStatement.setString(1, tipo);
                getStatement.setInt(2, id);
                ResultSet res = getStatement.executeQuery();
                return res.next();
            } else {
                return false;
            }
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }
        return false;
    }
}
