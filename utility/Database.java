package utility;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple Class to hold various database related methods and information.
 *
 * @author Shadab Mustafa
 */
public class Database {

    private static final String DBNAME = "WJ07DuS";
    private static final String URL = "jdbc:mysql://wgudb.ucertify.com/" + DBNAME;
    private static final String USER = "U07DuS";
    private static final String PASS = "53688995054";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static Connection conn;

    public Database() {}
/**
 * Connects to Database
 */
    public static void connect() {
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to MySQL Database");
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }

    /**
 *     Shuts Database Connection
     */
    public static void disconnect() {
        try {
            conn.close();
            System.out.println("Disconnected From MySQL Database");
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    /**
 *     Simple getter that Returns Database Connection
     * @return conn, returns connection
     */
    public static Connection getConnection() {
        return conn;
    }

}