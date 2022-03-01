package View_Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages changing view from the log in screen to the main view screen.
 * @author Shadab Mustafa
 */
public final class ViewController extends MiscLambdaController {
    private final Scene scene;
    private final Stage primaryStage;

    public ViewController(final Scene scene, final Stage primaryStage) {
        this.scene = scene;
        this.primaryStage = primaryStage;
    }

    /**
     * Displays the log in view so the user can log in
     *
     * @throws Exception any exception within the scene building
     */
    public void showLoginView() throws Exception {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/Login.fxml"), bundle);
        scene.setRoot(loader.load());
        loader.<LoginController>getController().setViewController(this);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
    }

    /**
     * After a successful login, this method is called to display the main view
     */
    public void showMainView() {
        try {
            primaryStage.hide();
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/Main.fxml"), bundle);
            scene.setRoot(loader.load());
            final MainController mainController = loader.getController();
            mainController.setViewController(this);
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("error opening main view:");
            System.out.println(e);
        }
    }
}