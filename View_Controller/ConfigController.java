package View_Controller;

import Models.Record;
import Models.Record.ValidationError;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * An abstract class which helps create functionality for all form configurations.
 * Defines various abstract methods, which must be further defined by various subclasses for proper
 * data processing.
 *
 * @author Shadab Mustafa
 *
 * @param <T> a subclass of the Record model that can be updated/created by the ConfigController subclass
 */
public abstract class ConfigController<T extends Record> extends MiscLambdaController implements Initializable {

    public ConfigController(String windowTitle, ConfigAssembler.Mode mode, T record, Function<T, Boolean> callback) {
        this.windowTitle = windowTitle;
        readOnly = mode == ConfigAssembler.Mode.Read;
        this.mode = mode;
        this.record = record;
        this.callback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (record.getId() != 0) idField.setText(Long.toString(record.getId()));
        idField.setDisable(true);
        if (mode != ConfigAssembler.Mode.Create) {
            if (mode == ConfigAssembler.Mode.Read) {
                buttonBar.setVisible(false);
            }
            setFields();
            setTextFields();
        }
    }

    /**
     * Launches a form to create/view/update a record
     *
     * @return this
     */
    public ConfigController<T> open() {
        openForm();
        return this;
    }

    /**
     * a wrapper around the callback lambda that allows idempotency via setting the lambda member to null after it's
     * first call
     *
     * @param record the record that is to be saved/modified
     * @return whether the window can close safely
     */
    private boolean callCallback(T record) {
        if (callback != null) {
            if (callback.apply(record)) {
                callback = null;
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Handles  actions necessary for saving a record to the database.
     * first, it applies values from form configuration to record
     * second, a callback occurs that ascertains whether the record has valid values
     *  third, a call the callback with the record occurs
     * fourth and finally, form is closed
     */
    @FXML
    private void handleSave() {
        try {
            for (Node button : buttonBar.getButtons()) {
                button.setDisable(true);
            }
            applyStringFormFieldsToRecord();
            applyOtherFieldsToRecord();
            record.validate();
            if (callCallback(record)) handleClose();
        } catch (ValidationError err) {
            displayError(err);
        }
        for (Node button : buttonBar.getButtons()) {
            button.setDisable(false);
        }
    }

    /**
     * String fields are automatically applied to the record via reflection, non-string fields can be applied to the
     * record by implementing the follwoing method in subclasses
     */
    abstract protected void applyOtherFieldsToRecord();

    /**
     * Is the cancel button action.
     */
    private void handleClose() {
        if (stage != null) stage.hide();
        stage = null;
    }

    /**
     * Closes the window without saving the record.
     *Used during cancel or x button usage
     * @param event an action event from JavaFX when the button is clicked
     */
    @FXML
    private void handleClose(ActionEvent event) {
        callCallback(null);
        handleClose();
    }

    /**
     * Applies the values from the record to the form configuration so that an existing record can be updated. when creating a new
     * record, this method isn't called and the form configuration values are left in their default state.
     */
    protected abstract void setFields();

    /**
     * Allows for subclasses to define the path to their FXML files for polymorphic forms.
     *
     * @return the resource url for the form FXML
     */
    protected abstract String getResourceURL();

    /**
     * Allows for dynamic setting of the title of the form window based on the current action
     *
     * @return the title to be set for the form window
     */
    private String getWindowTitle() {
        return windowTitle;
    }

    /**
     * @return the width of the form window
     */
    protected abstract double getWidth();

    /**
     * @return the height of the form window
     */
    protected abstract double getHeight();

    /**
     * lambda: Guarantees the callback is always called
     * <p>
     * Launches a new window with the correct form for the controller
     */
    private void openForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getResourceURL()), bundle);
            loader.setController(this);
            Scene scene = new Scene(loader.load(), getWidth(), getHeight());
            stage = new Stage();
            // ensures the callback is always called
            stage.setOnHidden(ev -> handleClose(null));
            stage.setScene(scene);
            stage.setTitle(getWindowTitle());
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            handleClose(null);
        }
    }

    /**
     * lambda: Repeats over all the fields. Very clean and readable
     * <p>
     * uses #iterateStringFields to set values from the record to the form
     *
     * @see ConfigController#iterateStringFields(BiConsumer)
     */
    private void setTextFields() {
        // Repeats over all the fields. Very clean and readable
        iterateStringFields((textField, recordField) -> {
            try {
                final String data = (String) recordField.get(record);
                textField.setText(data);
                textField.setDisable(readOnly);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * lambda: Repeats over all the fields. Very clean and readable
     * <p>
     * uses #iterateStringFields to set values from the form to the record
     *
     * @see ConfigController#iterateStringFields(BiConsumer)
     */
    private void applyStringFormFieldsToRecord() {
        // Repeats over all the fields. Very clean and readable
        iterateStringFields((textField, recordField) -> {
            try {
                recordField.set(record, textField.getText().trim());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Utilizes reflection to find all the members of a record that are strings
     *
     * @return a list of string Fields of the record
     */
    private List<Field> getStringFields() {
        final List<Field> output = new ArrayList<>();
        for (Field declaredField : record.getClass().getDeclaredFields()) {
            if (declaredField.getType() == String.class) {
                output.add(declaredField);
            }
        }
        return output;
    }

    /**
     * Utilizes reflection to repeat over form configuration TextFields and record members to find matching fields and returns
     * the matching pairs for further processing
     *
     * @param callback a lambda expression for processing the TextField and its matching member in the record
     * @see ConfigController#applyStringFormFieldsToRecord()
     * @see ConfigController#setTextFields()
     */
    private void iterateStringFields(BiConsumer<TextField, Field> callback) {
        for (final Field declaredField : getStringFields()) {
            try {
                declaredField.setAccessible(true);
                final String fieldName = declaredField.getName();
                final Field textFieldField = getClass().getDeclaredField(String.format("%sField", fieldName));
                textFieldField.setAccessible(true);
                final TextField input = (TextField) textFieldField.get(this);
                callback.accept(input, declaredField);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Various variable and field declarations
     */
    private final ConfigAssembler.Mode mode;
    private final String windowTitle;
    @FXML
    protected TextField idField;
    protected T record;
    protected boolean readOnly = true;
    protected Function<T, Boolean> callback;
    @FXML
    private ButtonBar buttonBar;
    private Stage stage;


    /**
     * Various variable and field declarations
     */
    protected long getRecordId(Record record) {
        return Optional.ofNullable(record).map(Record::getId).orElse(0L);
    }
}