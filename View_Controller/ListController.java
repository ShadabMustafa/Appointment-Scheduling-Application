package View_Controller;

import Models.Model;
import Models.Record;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Abstract class holds records from database in table list format.
 * @author Shadab Mustafa
 * @param <T> a subclass of the Record model that implements the Model interface
 */
public abstract class ListController<T extends Record & Model<T>> extends MiscLambdaController implements Initializable {


    public ListController(ConfigAssembler configAssembler, MainController.EventEmitter eventEmitter) {
        this.configAssembler = configAssembler;
        this.eventEmitter = eventEmitter;
    }

    /**
     * adds columns to the table that match the shape of the generic T
     */
    protected abstract void addColumns();

    /**
     * lambda:  Set the string value in the table
     * <p>
     * Utilizes introspection to get all the string fields for the given generic mode T
     *
     * @param tClass    the class for T
     * @param fieldName the name of the instance field
     * @return the TableColumn for the record
     */
    protected TableColumn<T, String> getStringColumn(Class<T> tClass, String fieldName) {
        try {
            final Field field = tClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            final String key = String.format("%s.%s", tClass.getSimpleName().toLowerCase(), field.getName());
            final TableColumn<T, String> column = new TableColumn<>(bundle.getString(key));
            column.setCellValueFactory(param -> {
                try {
                    return new SimpleStringProperty((String) field.get(param.getValue()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            });
            return column;
        } catch (NoSuchFieldException ex) {
            System.out.println(ex);
        }

        return null;
    }

    /**
     * Implemented by subclasses to fill the table list with the data,and build maps that are necessary to properly
     * display foreign key columns
     */
    protected abstract void populateData();

    /**
     * lambda: Guarantees the long values are properly displayed
     *
     * @see Initializable#initialize(URL, ResourceBundle)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterButton.setDisable(true);
        filterButton.setVisible(false);
        final TableColumn<T, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    /**
     * Launches a form in the proper mode with the given record
     *
     * @param mode     the mode for the form
     * @param record   the record to display in the form
     * @param callback the callback to consume the record after editing has finished
     */
    private void openForm(ConfigAssembler.Mode mode, T record, Function<T, Boolean> callback) {
        configController = configAssembler.getInstance(mode, record, callback);
        configController.open();
    }

    /**
     * Refreshes the table list view and sets the configController null after it is closed. Only one form maybe open at
     * a time.
     */
    private void finalizeAction() {
        tableView.refresh();
        configController = null;
    }

    /**
     * lambda: Absorb an exception and result set and used for Resource clean up.
     * <p>
     * Execute a SQL insert statement for the given record
     *
     * @param record the record to insert
     * @return whether the form can close or not
     */
    private boolean addToDatabase(T record) {
        final boolean updatable = canUpdate(record);
        if (updatable) {
            final List<Object> arguments = record.toValues();
            arguments.add(userId);
            arguments.add(userId);
            executeInsert(getInsertStatement(), arguments, (ex, newId) -> {
                if (ex != null) printSQLException(ex);
                if (newId != null) record.setId(newId);
            });
        }

        return updatable;
    }

    /**
     * @return a string with SQL insert statement for a record
     */
    protected abstract String getInsertStatement();

    /**
     * @return a blank record to use to hold the data for a new record before it is saved to the databases
     */
    protected abstract T getNewRecord();

    /**
     * lambda: Launches the form and registers a callback to be called with the completed record
     * <p>
     * Launches the form with a blank record and saves it into the database.
     */
    @FXML
    private void addRecord() {
        if (configController == null) {
            openForm(ConfigAssembler.Mode.Create, getNewRecord(), (newRecord) -> {
                final boolean recordHandledCorrectly = newRecord == null || addToDatabase(newRecord);
                if (recordHandledCorrectly) {
                    if (newRecord != null && newRecord.getId() != 0) {
                        tableView.getItems().add(newRecord);
                    }

                    finalizeAction();
                }
                return recordHandledCorrectly;
            });
        }
    }

    private T getSelectedRecord() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    /**
     * lambda: Launches the form and registers a callback to be called with the completed record
     * <p>
     * Launches the selected record in view mode
     */
    @FXML
    private void viewRecord() {
        final T selected = getSelectedRecord();
        if (selected != null && configController == null) {
            openForm(ConfigAssembler.Mode.Read, selected, (record) -> {
                finalizeAction();
                return true;
            });
        }
    }

    /**
     * lambda: Absorb an exception and result set and used for Resource clean up.
     * <p>
     * Launches the edit form and saves the changes to the database
     *
     * @param record the record to update
     * @return whether the form can close or not
     */
    protected boolean updateInDatabase(T record) {
        final boolean updatable = canUpdate(record);
        if (updatable) {
            final List<Object> arguments = record.toValues();
            arguments.add(userId);
            arguments.add(record.getId());
            executeLambdaUpdate(getUpdateStatement(), arguments, (ex, updateCount) -> {
                if (ex != null) printSQLException(ex);
                if (updateCount == 1) getSelectedRecord().applyChanges(record);
            });
        }
        return updatable;
    }

    /**
     * performs SQL validations on the record to ensure it is valid
     *
     * @param record the record to update
     * @return whether the record can be updated
     */
    protected abstract boolean canUpdate(T record);

    /**
     * @return a string that contains a SQL statement to update a record
     */
    protected abstract String getUpdateStatement();

    /**
     * lambda: opens the form and registers a callback to be called with the completed record
     * <p>
     * Launches the form with the given record and executes a SQL statement to update a record in the database
     */
    @FXML
    private void editRecord() {
        final T selected = getSelectedRecord();
        if (selected != null && configController == null) {
            openForm(ConfigAssembler.Mode.Update, selected.copy(), (updatedRecord) -> {
                final boolean recordHandledCorrectly = updatedRecord == null || updateInDatabase(updatedRecord);
                if (recordHandledCorrectly) finalizeAction();
                return recordHandledCorrectly;
            });
        }
    }

    /**
     * Converts list of arguments into a list of objects
     *
     * @param values the values to include in the list
     * @return a list of the values
     */
    protected List<Object> toArray(Object... values) {
        final List<Object> output = new ArrayList<>();
        if (values != null) {
            Collections.addAll(output, values);
        } else {
            output.add(null);
        }

        return output;
    }

    /**
     * lambda: Absorb an exception and result set and used for Resource clean up.
     * <p>
     * Execute a SQL statement to delete a record from the database
     *
     * @param record the record delete
     */
    protected void deleteFromDatabase(T record) {
        if (deleteDependencies(record)) {
            executeLambdaUpdate(getDeleteStatement(), toArray(record.getId()), (ex, updates) -> {
                if (ex != null) printSQLException(ex);
                if (updates == 1) record.setId(0);
            });
        }
    }

    /**
     * Deletes dependencies for  given record. Called upon before attempting to delete the given record
     *
     * @param record the record whose dependencies need to be deleted
     * @return whether the dependent records could be deleted successfully
     */
    protected abstract boolean deleteDependencies(T record);

    /**
     * @return a SQL statement that can delete a record from a table
     */
    protected abstract String getDeleteStatement();

    @FXML
    protected void deleteRecord() {
        final T recordToDelete = getSelectedRecord();
        if (recordToDelete != null) {
            deleteButton.setDisable(true);
            final String message = getDeletedMessage(recordToDelete);
            deleteFromDatabase(recordToDelete);
            if (recordToDelete.getId() == 0) {
                tableView.getItems().remove(recordToDelete);
                tableView.refresh();
                displayAlert(bundle.getString("record.deleted.title"), message, Alert.AlertType.INFORMATION);
                emitEvent();
            }
            deleteButton.setDisable(false);
        }
    }

    protected void emitEvent() {
    }

    protected abstract String getDeletedMessage(T record);


    public ObservableList<T> getData() {
        return tableView.getItems();
    }

    /**
     * adds a filter to the table, only used by the ApptListController
     */
    @FXML
    protected void addFilter() {
    }


    /**
     * Various variables and fields declared.
     */
    final protected ConfigAssembler configAssembler;
    final protected MainController.EventEmitter eventEmitter;
    @FXML
    protected TableView<T> tableView;
    @FXML
    protected Button filterButton;
    protected ConfigController<T> configController;
    @FXML
    private Button deleteButton;


}