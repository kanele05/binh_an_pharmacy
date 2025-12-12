package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static Connection con = null;
    private static final ConnectDB instance = new ConnectDB();

    private ConnectDB() {

    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException {
        String url = "jdbc:sqlserver://localhost:14330;databaseName=QLTHUOC;trustServerCertificate=true;";
        String user = "sa";
        String password = "sapassword";
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(url, user, password);
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                con = null;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            instance.connect();
        }
        return con;
    }
}
