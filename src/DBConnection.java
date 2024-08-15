import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/interdisciplinar";
    private static final String USER = "root";
    private static final String PASSWORD = "unesc";

    public static Connection getConnection() {
        try {
            return tryConnection(URL, USER, PASSWORD, "interdisciplinar");
        } catch (SQLException e) {
            ErrorHandler.handleError(e);

            Object[] options = {"Sim", "Não"};
            int initialResult = JOptionPane.showOptionDialog(null,
                    "As credenciais padrão do banco de dados falharam. Gostaria de continuar a execução com outras?",
                    "Conexão Falhou",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (initialResult == JOptionPane.YES_OPTION) {
                JTextField ip = new JTextField();
                JTextField schema = new JTextField();
                JTextField usr = new JTextField();
                JPasswordField pass = new JPasswordField();

                Object[] message = {
                        "IP: (Ex: \"localhost:3306\")", ip,
                        "Schema:", schema,
                        "Usuário:", usr,
                        "Senha:", pass
                };

                int result = JOptionPane.showConfirmDialog(null, message, "Insira as credenciais", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    String url = "jdbc:mysql://" + ip.getText() + "/" + schema.getText();
                    String usu = usr.getText();
                    String sen = new String(pass.getPassword());

                    try {
                        return tryConnection(url, usu, sen, schema.getText());
                    } catch (SQLException ex) {
                        ErrorHandler.handleError(ex);
                        JOptionPane.showMessageDialog(null, "Conexão com o banco de dados falhou. O execução será interrompida.");
                        System.out.println("Novas credenciais inválidas.");
                        throw new RuntimeException("Conexão com o banco de dados falhou.");
                    }


                } else {
                    System.out.println("O Usuario interrompeu a execução.");
                    System.exit(0);
                }
            } else {
                System.out.println("O Usuario interrompeu a execução.");
                System.exit(0);
            }
        }
        return null;
    }

    private static Connection tryConnection(String url, String usuario, String senha, String schema) throws SQLException {
        Connection con = DriverManager.getConnection(url, usuario, senha);
        PreparedStatement useSchema = con.prepareStatement("USE " + schema);
        useSchema.execute();
        return con;
    }
}
