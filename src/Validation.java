import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Validation {
    private final Connection connection;
    private int[] countArray = new int[3];
    private List<Pokemon> retrivedPokemons = new ArrayList<>();
    private List<Pokemon> pokemonsInTypeTables = new ArrayList<>();
    private List<Pokemon> deletedPokemonsDB = new ArrayList<>();
    private boolean[] validationResults = {false, false, false, false, false};

    public Validation(Connection con) {
        this.connection = con;
    }

    public void loadDB() {
        try {
            assert connection != null;

            PreparedStatement validationStatement = connection.prepareStatement("CALL getValidationInfo()");
            boolean results = validationStatement.execute();

            if (results) {
                ResultSet rs = validationStatement.getResultSet();
                if (rs.next()) {
                    int countEletrico = rs.getInt("quantidade_eletrico");
                    int countFogo = rs.getInt("quantidade_fogo");
                    int countVoador = rs.getInt("quantidade_voador");

                    countArray[0] = countEletrico;
                    countArray[1] = countFogo;
                    countArray[2] = countVoador;
                } else {
                    System.out.println("Loading de quantidade falhou.");
                }

                results = validationStatement.getMoreResults();
            } else {
                throw new RuntimeException("Erro! procedure de validação não retornou resultados.");
            }

            if (results) {
                ResultSet rs = validationStatement.getResultSet();
                while (rs.next()) {
                    String nome = Utils.normalizeAcento(rs.getString("pokemon")).toLowerCase();
                    String tipoPoke = Utils.normalizeAcento(rs.getString("tipo")).toLowerCase();
                    retrivedPokemons.add(new Pokemon(rs.getInt("id"), nome, tipoPoke));
                }

                results = validationStatement.getMoreResults();
            } else {
                throw new RuntimeException("Erro! procedure de validação não retornou resultados.");
            }

            if (results) {
                ResultSet rs = validationStatement.getResultSet();
                while (rs.next()) {
                    String nome = Utils.normalizeAcento(rs.getString("pokemon")).toLowerCase();
                    String tipoPoke = Utils.normalizeAcento(rs.getString("tipo")).toLowerCase();
                    pokemonsInTypeTables.add(new Pokemon(rs.getInt("id"), nome, tipoPoke));
                }

                results = validationStatement.getMoreResults();
            } else {
                throw new RuntimeException("Erro! procedure de validação não retornou resultados.");
            }

            if (results) {
                ResultSet rs = validationStatement.getResultSet();
                while (rs.next()) {
                    String nome = Utils.normalizeAcento(rs.getString("pokemon")).toLowerCase();
                    String tipoPoke = Utils.normalizeAcento(rs.getString("tipo")).toLowerCase();
                    deletedPokemonsDB.add(new Pokemon(rs.getInt("id"), nome, tipoPoke));
                }
            } else {
                throw new RuntimeException("Erro! procedure de validação não retornou resultados.");
            }
        } catch (SQLException | AssertionError | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean[] validateDB() {
        DBGet dbGet = new DBGet(connection);

        List<Quantifier> quantificadores = dbGet.getCountTable();
        int countEletrico = countArray[0];
        int countFogo = countArray[1];
        int countVoador = countArray[2];

        boolean[] valid = new boolean[3];
        for (Quantifier q : quantificadores) {
            switch (q.getTipo()) {
                case "eletrico":
                    valid[0] = q.getQuantidade() == countEletrico;
                    break;
                case "fogo":
                    valid[1] = q.getQuantidade() == countFogo;
                    break;
                case "voador":
                    valid[2] = q.getQuantidade() == countVoador;
                    break;
            }
        }

        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                System.out.println("Validação do numero de " + quantificadores.get(i + 1).getTipo() + " passou.");
                switch (quantificadores.get(i + 1).getTipo()) {
                    case "fogo":
                        validationResults[0] = true;
                        break;
                    case "eletrico":
                        validationResults[1] = true;
                        break;
                    case "voador":
                        validationResults[2] = true;
                        break;
                }
            } else {
                System.out.println("Validação do numero de " + quantificadores.get(i + 1).getTipo() + " falhou.");
            }
        }

        List<Pokemon> deletedPokes = new ArrayList<>();
        List<Pokemon> expectedPokes = new ArrayList<>();
        expectedPokes.add(new Pokemon("picachu", "eletrico"));
        expectedPokes.add(new Pokemon("miraidon", "eletrico"));
        expectedPokes.add(new Pokemon("charmander", "Fogo"));
        expectedPokes.add(new Pokemon("fuecoco", "Fogo"));
        expectedPokes.add(new Pokemon("miraidon", "elétrico"));
        expectedPokes.add(new Pokemon("pidgeotto", "voador"));
        expectedPokes.add(new Pokemon("butterfree", "voador"));
        expectedPokes.add(new Pokemon("butterfree", "voador"));
        expectedPokes.add(new Pokemon("fuecoco", "fogo"));

        Set<String> seenNames = new HashSet<>();
        for (Pokemon poke : expectedPokes) {
            boolean found = false;
            for (Pokemon vPoke : retrivedPokemons) {
                if (Utils.equalsInsensitive(poke.getNome(), vPoke.getNome()) && Utils.equalsInsensitive(poke.getTipo(), vPoke.getTipo())) {
                    if (!seenNames.contains(poke.getNome())) {
                        found = true;
                        seenNames.add(poke.getNome());
                        break;
                    }
                }
            }
            if (!found) {
                deletedPokes.add(poke);
            }
        }

        int deletedCount = 0;
        for (Pokemon deletedDBPoke : deletedPokemonsDB) {
            for (Pokemon deletedPoke : deletedPokes) {
                if (Utils.equalsInsensitive(deletedDBPoke.getNome(), deletedPoke.getNome()) && Utils.equalsInsensitive(deletedDBPoke.getTipo(), deletedPoke.getTipo())) {
                    deletedCount++;
                }
            }
        }

        if (deletedCount == deletedPokes.size()) {
            System.out.println("Validação de integridade dos dados deletados passou.");
            validationResults[3] = true;
        } else {
            System.out.println("Validação de integridade dos dados deletados falhou.");
        }

        boolean noDuplicates = true;
        Set<String> seenNamesType = new HashSet<>();
        for (Pokemon poke : pokemonsInTypeTables) {
            if (!seenNamesType.contains(poke.getNome())) {
                seenNamesType.add(poke.getNome());
            } else {
                noDuplicates = false;
                break;
            }
        }

        if (noDuplicates) {
            System.out.println("Validação da não duplicidade das tabelas de tipo passou.");
            validationResults[4] = true;
        } else {
            System.out.println("Validação da não duplicidade das tabelas de tipo falhou.");
        }

        return validationResults;
    }
}

