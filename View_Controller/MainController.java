package View_Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Used for tab switching and data filling purposes.
 * @author Shadab Mustafa
 */

public class MainController extends MiscLambdaController implements Initializable {

    /**
     * lambda: Determines which tab has been chosen and
     * subsequently displays the correct data
     *
     * @see Initializable#initialize(URL, ResourceBundle)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tabPane.getSelectionModel().selectedItemProperty()
                .addListener(((observableValue, oldTab, newTab) -> populateData(newTab)));
        populateData(tabPane.getSelectionModel().getSelectedItem());
    }

    /**
     * Triggered whenever the tab on the main view changes, guarantees that all the tabs are initialized
     *
     * @param newTab the currently selected tab
     */
    private void populateData(Tab newTab) {
        if (newTab == customerTab) {
            populateCustomerData();
        } else if (newTab == appointmentTab) {
            populateAppointmentData();
        }
    }

    /**
     * Creates the customer table list if it's not already initialized.
     */
    private void populateCustomerData() {
        if (customerTabInitialized) return;
        customerTabInitialized = true;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/List.fxml"), bundle);
        custListControllerController = new CustListController(eventEmitter);
        loader.setController(custListControllerController);
        try {
            customerTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Creates the appointment table list if it's not already initialized
     */
    private void populateAppointmentData() {
        if (appointmentTabInitialized) return;
        appointmentTabInitialized = true;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/List.fxml"), bundle);
        loader.setController(new ApptListController(custListControllerController.getData(), eventEmitter));
        try {
            appointmentTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    public enum Event {
        CustomerDeleted
    }

    /**
     * Event emitter class used by customer table list controller to alert the appointment table list of a customer deletion so the
     * deleted appointments can be removed from the table list without errors.
     */
    final public class EventEmitter implements java.util.EventListener {
        final private HashMap<Event, List<Runnable>> eventMap = new HashMap<>();

        /**
         * registers an event listener
         *
         * @param e the event to listen to
         * @param r a callback for when the event happens
         */
        public void addListener(Event e, Runnable r) {
            List<Runnable> listeners = eventMap.get(e);
            if (listeners == null) {
                listeners = new ArrayList<>();
                eventMap.put(e, listeners);
            }
            listeners.add(r);
        }

        /**
         * calls all registered event listeners for the emitted event
         *
         * @param e the event that happened
         */
        public void emit(Event e) {
            final List<Runnable> listeners = eventMap.get(e);
            if (listeners != null) {
                for (Runnable runnable : listeners) {
                    runnable.run();
                }
            }
        }
    }


    /**
     * Various variables and fields declared.
     */
    private final EventEmitter eventEmitter = new EventEmitter();
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab customerTab;
    @FXML
    private Tab appointmentTab;
    private boolean customerTabInitialized = false;
    private boolean appointmentTabInitialized = false;
    private CustListController custListControllerController;

}