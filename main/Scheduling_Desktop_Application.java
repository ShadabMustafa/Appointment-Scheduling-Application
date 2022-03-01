package main;

import View_Controller.MiscLambdaController;
import View_Controller.ViewController;
import Models.Record;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utility.Database;

import java.util.Locale;

/**
 * Starts the program.
 *
 * @author Shadab Mustafa
 */

public class Scheduling_Desktop_Application extends Application {

    public static void main(String[] args) {
        Database.connect();
        launch(args);
        Database.disconnect();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MiscLambdaController.setLocaleAndBundle();
        Record.bundle = MiscLambdaController.getBundle();
        Record.locale = MiscLambdaController.getLocale();
        Locale.setDefault(MiscLambdaController.getLocale());
        final Scene scene = new Scene(new StackPane());

        ViewController viewController = new ViewController(scene, primaryStage);
        viewController.showLoginView();

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}