import java.sql.*;

public class DBTables {
    public final Connection connection;

    public DBTables(Connection con) {
        this.connection = con;
    }

    public void createTables() {
        try {
            assert connection != null;

            PreparedStatement dropPoke = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemons");
            PreparedStatement dropFire = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemon_fogo");
            PreparedStatement dropElectric = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemon_eletrico");
            PreparedStatement dropFlying = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemon_voador");
            PreparedStatement dropCount = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemon_totalizador");
            PreparedStatement dropDeleted = connection.prepareStatement("DROP TABLE IF EXISTS tb_pokemon_deletados");

            dropPoke.executeUpdate();
            dropFire.executeUpdate();
            dropElectric.executeUpdate();
            dropFlying.executeUpdate();
            dropCount.executeUpdate();
            dropDeleted.executeUpdate();

            PreparedStatement createPoke = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon ( id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, pokemon VARCHAR(50) NOT NULL, tipo VARCHAR(10) NOT NULL );");
            PreparedStatement createFire = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon_fogo ( id INT NOT NULL, CONSTRAINT tb_pokemon_fogo_id_foreign_key FOREIGN KEY (id) REFERENCES tb_pokemon (id) );");
            PreparedStatement createElectric = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon_eletrico ( id INT NOT NULL, CONSTRAINT tb_pokemon_eletrico_id_foreign_key FOREIGN KEY (id) REFERENCES tb_pokemon (id) );");
            PreparedStatement createFlying = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon_voador ( id INT NOT NULL, CONSTRAINT tb_pokemon_voador_id_foreign_key FOREIGN KEY (id) REFERENCES tb_pokemon (id) );");
            PreparedStatement createCount = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon_totalizador ( id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, tipo VARCHAR(10) NOT NULL, quantidade INT NOT NULL );");
            PreparedStatement createDeleted = connection.prepareStatement("CREATE TABLE IF NOT EXISTS tb_pokemon_deletados ( id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, pokemon VARCHAR(50) NOT NULL, tipo VARCHAR(10) NOT NULL );");

            createPoke.executeUpdate();
            createFire.executeUpdate();
            createElectric.executeUpdate();
            createFlying.executeUpdate();
            createCount.executeUpdate();
            createDeleted.executeUpdate();

            setupCountTypes();

            PreparedStatement deleteProcedure = connection.prepareStatement("DROP PROCEDURE IF EXISTS getValidationInfo");
            deleteProcedure.executeUpdate();

            PreparedStatement createProcedure = connection.prepareStatement("CREATE PROCEDURE getValidationInfo() BEGIN SELECT (SELECT COUNT(*) FROM tb_pokemon_eletrico) AS quantidade_eletrico, (SELECT COUNT(*) FROM tb_pokemon_fogo) AS quantidade_fogo, (SELECT COUNT(*) FROM tb_pokemon_voador) AS quantidade_voador; SELECT id, pokemon, tipo FROM tb_pokemon; SELECT tb_pokemon.id, pokemon, tipo FROM tb_pokemon_eletrico JOIN tb_pokemon ON tb_pokemon_eletrico.id = tb_pokemon.id UNION ALL SELECT tb_pokemon.id, pokemon, tipo FROM tb_pokemon_fogo JOIN tb_pokemon ON tb_pokemon_fogo.id = tb_pokemon.id UNION ALL SELECT tb_pokemon.id, pokemon, tipo FROM tb_pokemon_voador JOIN tb_pokemon ON tb_pokemon_voador.id = tb_pokemon.id; SELECT id, pokemon, tipo FROM tb_pokemon_deletados; END;");
            createProcedure.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupCountTypes() throws SQLException {
        PreparedStatement getCount = connection.prepareStatement("SELECT * FROM tb_pokemon_totalizador");
        ResultSet count = getCount.executeQuery();

        String[] expectedCountTypes = {"duplicados", "fogo", "eletrico", "voador"};

        int size = 0;
        boolean countTypesExists = true;
        while (count.next()) {
            size++;
            String tipo = count.getString("tipo");
            if (!tipo.equals(expectedCountTypes[0]) && !tipo.equals(expectedCountTypes[1]) && !tipo.equals(expectedCountTypes[2]) && !tipo.equals(expectedCountTypes[3])) {
                countTypesExists = false;
                break;
            }
        }

        if (size == 0) {
            countTypesExists = false;
        } else if (size < 4) {
            countTypesExists = false;

            PreparedStatement delete = connection.prepareStatement("DELETE FROM tb_pokemon_totalizador WHERE TRUE");
            PreparedStatement resetIncrement = connection.prepareStatement("ALTER TABLE tb_pokemon_totalizador AUTO_INCREMENT = 1");
            delete.executeUpdate();
            resetIncrement.executeUpdate();
        }

        if (!countTypesExists) {
            PreparedStatement insertCount = connection.prepareStatement("INSERT INTO tb_pokemon_totalizador (tipo, quantidade) VALUES (?, ?)");
            for (String tipo : expectedCountTypes) {
                insertCount.setString(1, tipo);
                insertCount.setInt(2, 0);
                insertCount.executeUpdate();
            }
        }
    }

    public boolean checkTables() {
        boolean result = false;

        try {
            assert connection != null;

            DatabaseMetaData dbm = connection.getMetaData();

            String[] expectedPokeColumns = {"id", "pokemon", "tipo"};
            boolean pokeColumnsExists = checkColumns(dbm, "tb_pokemon", expectedPokeColumns);

            String[] expectedFireColumns = {"id"};
            boolean fireColumnsExists = checkColumns(dbm, "tb_pokemon_fogo", expectedFireColumns);

            String[] expectedElectricColumns = {"id"};
            boolean electricColumnsExists = checkColumns(dbm, "tb_pokemon_eletrico", expectedElectricColumns);

            String[] expectedFlyingColumns = {"id"};
            boolean flyingColumnsExists = checkColumns(dbm, "tb_pokemon_voador", expectedFlyingColumns);

            String expectedDeletedColumns[] = {"id", "pokemon", "tipo"};
            boolean deletedColumnsExists = checkColumns(dbm, "tb_pokemon_deletados", expectedDeletedColumns);

            String[] expectedCountColumns = {"id", "tipo", "quantidade"};
            boolean countColumnsExists = checkColumns(dbm, "tb_pokemon_totalizador", expectedCountColumns);

            if (countColumnsExists) {
                setupCountTypes();
            }

            result = pokeColumnsExists && fireColumnsExists && electricColumnsExists && flyingColumnsExists && deletedColumnsExists && countColumnsExists;
        } catch (SQLException | AssertionError e) {
            ErrorHandler.handleError(e);
        }

        return result;
    }

    private boolean checkColumns(DatabaseMetaData dbm, String tableName, String[] expectedColumns) throws SQLException {
        if (!checkTable(dbm, tableName)) {
            return false;
        }

        ResultSet columns = dbm.getColumns(null, null, tableName, null);

        boolean columnsExists = true;
        int executed = 0;
        while (columns.next()) {
            executed++;
            String columnName = columns.getString("COLUMN_NAME");
            if (!columnName.equals(expectedColumns[0]) && !columnName.equals(expectedColumns[1]) && !columnName.equals(expectedColumns[2])) {
                columnsExists = false;
                break;
            }
        }

        if (executed == 0) {
            columnsExists = false;
        }

        return columnsExists;
    }

    private boolean checkTable(DatabaseMetaData dbm, String tableName) throws SQLException {
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }
}
