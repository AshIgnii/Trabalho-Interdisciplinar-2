import javax.swing.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Connection con = DBConnection.getConnection();
        DBGet dbGet = new DBGet(con);
        DBSet dbSet = new DBSet(con, dbGet);
        DBTables dbTables = new DBTables(con);
        
        boolean tablesExist = dbTables.checkTables();
        if (!tablesExist) {
            String[] options = {"Sim", "Não"};
            int create = JOptionPane.showOptionDialog(null,
                    "As tabelas não existem. Deseja criá-las?",
                    "Tabelas não existem",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (create == JOptionPane.YES_OPTION) {
                dbTables.createTables();
            } else {
                System.out.println("Tabelas não existem. A execução será interrompida.");
                throw new RuntimeException("Tabelas não existem.");
            }
        }
        List<Pokemon> pokemons = new ArrayList<>();
        pokemons.add(new Pokemon("picachu", "eletrico"));
        pokemons.add(new Pokemon("miraidon", "eletrico"));
        pokemons.add(new Pokemon("charmander", "Fogo"));
        pokemons.add(new Pokemon("fuecoco", "Fogo"));
        pokemons.add(new Pokemon("miraidon", "elétrico"));
        pokemons.add(new Pokemon("pidgeotto", "voador"));
        pokemons.add(new Pokemon("butterfree", "voador"));
        pokemons.add(new Pokemon("butterfree", "voador"));
        pokemons.add(new Pokemon("fuecoco", "fogo"));
        
        dbSet.insertPokemon(pokemons);

        System.out.println("-----Conteúdo depois do insert-----" + "\n" +
                dbGet.getPokemons() + "\n" +
                dbGet.getPokemons("fogo") + "\n" +
                dbGet.getPokemons("eletrico") + "\n" +
                dbGet.getPokemons("voador") + "\n" +
                dbGet.getCountTable()
        );

        dbSet.deleteDuplicatesInMainTable();

        System.out.println("\n -----Conteúdo depois de remover os duplicados-----" + "\n" +
                dbGet.getPokemons() + "\n" +
                dbGet.getCountTable()
        );

        System.out.println("\n -----Validação-----");

        Validation validator = new Validation(con);
        validator.loadDB();
        boolean[] results = validator.validateDB();

        JOptionPane.showMessageDialog(null,
                "Validação do numero de pokemons: " + "\n" +
                        "   Elétricos: " + (results[0] ? "Passou ✅" : "Não passou ❌") + "\n" +
                        "   Fogo: " + (results[1] ? "Passou ✅" : "Não passou ❌") + "\n" +
                        "   Voador: " + (results[2] ? "Passou ✅" : "Não passou ❌") + "\n\n" +
                        "Validação de integridade dos dados deletados: " + (results[3] ? "Passou ✅" : "Não passou ❌") + "\n\n" +
                        "Validação da não duplicidade das tabelas de tipo: " + (results[4] ? "Passou ✅" : "Não passou ❌") + "\n\n" +
                        "Para mais informações, consulte o console."
        );

        String[] options = {"Sim", "Não"};
        int reset = JOptionPane.showOptionDialog(null,
                "Deseja excluir o conteudo de todas as tabelas?",
                "Limpar tabelas",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (reset == JOptionPane.YES_OPTION) {
            System.out.print("\n");
            dbSet.purgeTables();
        }
    }
}