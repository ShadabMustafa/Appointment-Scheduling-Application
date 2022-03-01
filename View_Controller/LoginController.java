package View_Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import utility.Database;
import Models.LoginUser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import static utility.Logger.logLoginAttempt;

/**
 * Utilized for login purposes.
 * @author Shadab Mustafa
 */
public final class LoginController extends MiscLambdaController implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        zoneLabel.setText(getLocale().toString());
    }

    /**
     * Various variables and fields declared.
     */
    private static LoginUser currentUser;


      //Various getters and setters.
    public static LoginUser getCurrentUser() {
        return currentUser;
    }


    /**
     *
     * @param LoginUserName, user name used for login purposes
     * @param LoginPassWord, user password for login purposes
     * @return whether login information is valid
     */

    public  Boolean login(String LoginUserName, String LoginPassWord) {
        try {
            Statement statement = Database.getConnection().createStatement();
            String query = "SELECT * FROM users WHERE User_Name= '" + LoginUserName + "' AND Password='"+ LoginPassWord +"'";
            ResultSet results = statement.executeQuery(query);
            if(results.next()) {
                currentUser = new LoginUser();
                currentUser.setLoginUserName(results.getString("User_Name"));
                currentUser.setLoginPassWord(results.getString("Password"));
                statement.close();
                logLoginAttempt(LoginUserName,true);
                return true;
            } else {
                logLoginAttempt(LoginUserName,false);
                return false;
            }
        }  catch (SQLException e) {
            printSQLException(e);
            return false;
        }
    }



    /**
     * Confirms that the required fields aren't empty and login infomration is correct.
     *
     * @param event JavaFX button press event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        final String LoginUserName = usernameField.getText();
        final String LoginPassWord = passwordField.getText();
        boolean validUser = login(LoginUserName, LoginPassWord);
        long userId =-1 ;

        if (validUser) {

            try {
                Statement statement = Database.getConnection().createStatement();
                String query = "SELECT User_ID FROM users WHERE User_Name= '" + LoginUserName + "' AND Password='" + LoginPassWord + "'";
                ResultSet results = statement.executeQuery(query);
                if (results.next()) {
                    userId = Integer.parseInt(results.getObject(1).toString());
                }
            } catch (SQLException e) {
                printSQLException(e);
            }

            if (userId != -1) {
                MiscLambdaController.userId = userId;
                viewController.showMainView();
            }

        } else {
            displayError(bundle.getString("error.invalidCredentials"));
        }
    }

    /**
     * Logs user in when the "Enter" key is pressed
     *
     * @param event JavaFX key event
     */
    @FXML
    private void handleEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
    }

    /**
     * Handles login button action.
     */
    @FXML
    public void handleLogsButton() {
        File file = new File("login_activity.txt");
        if(file.exists()) {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    System.out.println("Error Opening Log File: " + e.getMessage());
                }
            }
        }
    }


    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label zoneLabel;



}