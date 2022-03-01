package View_Controller;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import Models.Record;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Helps setup an appointment table list
 *
 * @author Shadab Mustafa
 */

public final class ApptListController extends ListController<Appointment> implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        filterButton.setDisable(false);
        filterButton.setVisible(true);
    }

    /**
     * lambda1:  converts a contact id into a contact name
     * lambda2:  converts a start time into the local time zone
     * lambda3:  converts an end time into the local time zone
     * lambda4:  displays a customer id, if possible
     *
     * @see ListController#addColumns()
     */
    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> contactCol = new TableColumn<>(bundle.getString("appointment.contact"));
        contactCol
                // lambda to correctly translate a contact id into a contact name
                .setCellValueFactory(param -> {
                    final Optional<Contact> contact = Optional.ofNullable(contactMap.get(param.getValue().getContactId()));
                    return new SimpleStringProperty(contact.map(Contact::getName).orElse(""));
                });
        final TableColumn<Appointment, String> startCol = new TableColumn<>(bundle.getString("appointment.start"));
        // lambda to correctly translate a start time into the local time zone
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        final TableColumn<Appointment, String> endCol = new TableColumn<>(bundle.getString("appointment.end"));
        // lambda to correctly translate an end time into the local time zone
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        final TableColumn<Appointment, String> customerIdCol = new TableColumn<>(bundle.getString("appointment.customerId"));
        // lambda to correctly display a customer id, if valid
        customerIdCol.setCellValueFactory(param -> new SimpleStringProperty(nonZero(param.getValue().getCustomerId())));
        tableView.getColumns().addAll(getStringColumn(Appointment.class, "title"),
                getStringColumn(Appointment.class, "description"),
                getStringColumn(Appointment.class, "location"),
                contactCol,
                getStringColumn(Appointment.class, "type"),
                startCol,
                endCol,
                customerIdCol);
    }

    /**
     * @see ListController#populateData()
     */
    @Override
    protected final void populateData() {
        populateTable();
        executeQuery("SELECT * FROM contacts", this::buildContactMap);
    }

    /**
     * fills the table with all pertinent appointment information. called via populateData() and the event emitter
     * listener whenever a customer is removed. Also applies the current filter if possible.
     */
    private void populateTable() {
        List<Object> arguments = null;
        String query = selectQuery;
        tableView.getItems().clear();
        if (currentFilter != null) {
            query += String.format(" WHERE YEAR(`Start`) = ? AND %s(`Start`) = ?", currentFilter.field);
            arguments = toArray(currentFilter.year, currentFilter.fieldValue);
        }
        executeQuery(query, arguments, this::parseAppointments);
    }

    /**
     * filters the results of an appointment query into instances of the appointment model for display purposes in the table
     *
     * @param exception a sql exception from the query
     * @param resultSet
     * the result set containing the appointment information rows
     */
    private void parseAppointments(SQLException exception, ResultSet resultSet
    ) {
        if (exception != null) return;
        final ObservableList<Appointment> appointments = tableView.getItems();
        appointments.clear();
        try {
            while (resultSet
                    .next()) {
                appointments.add(new Appointment(resultSet
                        .getLong(1),
                        resultSet
                                .getString(2),
                        resultSet
                                .getString(3),
                        resultSet
                                .getString(4),
                        resultSet
                                .getString(5),
                        resultSet
                                .getTimestamp(6).toLocalDateTime(),
                        resultSet
                                .getTimestamp(7).toLocalDateTime(),
                        resultSet
                                .getLong(8),
                        resultSet
                                .getLong(9),
                        resultSet
                                .getLong(10)));
            }
        } catch (SQLException exceptionSQL) {
            printSQLException(exceptionSQL);
        }
    }

    /**
     * Assembles a map of contactId to contact model instances. used to search the contact name from an appointment record
     * then the contact name is shown in the table list
     *
     * @param exception a sql exception from the query
     * @param resultSet
     * the result set containing the contact rows information
     */
    private void buildContactMap(SQLException exception, ResultSet resultSet
    ) {
        if (exception != null) return;
        try {
            while (resultSet
                    .next()) {
                final Contact contact = new Contact(resultSet
                        .getLong(1), resultSet
                        .getString(2), resultSet
                        .getString((3)));
                contactMap.put(contact.getId(), contact);
            }
        } catch (SQLException exceptionSQL) {
            printSQLException(exceptionSQL);
        }
    }

    /**
     * @see ListController#getInsertStatement()
     */
    @Override
    protected String getInsertStatement() {
        return "INSERT INTO appointments (Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID, Created_By, Last_Updated_By) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * @see ListController#getNewRecord()
     */
    @Override
    protected Appointment getNewRecord() {
        return new Appointment(0, null, null, null, null, null, null, 0, 0, 0);
    }

    /**
     * @see ListController#getUpdateStatement()
     */
    @Override
    protected String getUpdateStatement() {
        return "UPDATE appointments " +
                "SET Title = ?, Description = ?, `Location` = ?, `Type` = ?, `Start` = ?, `End` = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ?, Last_Updated_By = ?, Last_Update = NOW() " +
                "WHERE Appointment_ID = ?";
    }

    /**
     * @see ListController#deleteDependencies(Record)
     */
    @Override
    protected boolean deleteDependencies(Appointment record) {
        return true;
    }

    /**
     * @see ListController#getDeleteStatement()
     */
    @Override
    protected String getDeleteStatement() {
        return "DELETE FROM appointments WHERE Appointment_ID = ?";
    }

    /**
     * @see ListController#getDeletedMessage(Record)
     */
    @Override
    protected String getDeletedMessage(Appointment appointment) {
        final String replacement = String.format("%s (%s: %d, %s: %s)",
                bundle.getString("appointment.appointment"),
                bundle.getString("record.id"),
                appointment.getId(),
                bundle.getString("appointment.type"),
                appointment.getType());
        return bundle.getString("record.deleted.message").replace("%{record}", replacement);

    }

    /**
     * if the forwarded in value is 0, it merely returns an empty string for display in the table, otherwise it converts the long to a string
     * @param val the long to stringify
     * @return the string value for the table
     */
    protected String nonZero(long val) {
        return val == 0 ? "" : Long.toString(val);
    }

    /**
     * lambda: records a callback via the filter controller so it is known when the filter can be applied
     *
     * @see ListController#addFilter()
     */
    @Override
    protected void addFilter() {
      //  lambda: records a callback via the filter controller so it is known when the filter can be applied
        filterController.openFilterWindow((fields) -> {
            currentFilter = fields;
            populateData();
        });
    }

    /**
     * lambda: absorbs an exception and result set and aids in resource cleanup
     * @see ListController#canUpdate(Record)
     */
    @Override
    protected boolean canUpdate(Appointment record) {
        String query = "SELECT COUNT(*) FROM appointments " +
                "WHERE (UNIX_TIMESTAMP(`START`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?)" +
                "OR UNIX_TIMESTAMP(`END`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?)) " +
                "AND Customer_ID = ?";
        final List<Object> arguments = toArray(record.getSQLStart(),
                record.getSQLEnd(),
                record.getSQLStart(),
                record.getSQLEnd(),
                record.getCustomerId());
        if (record.getId() != 0L) {
            query += " AND Appointment_Id != ?";
            arguments.add(record.getId());
        }
        // lambda which absorbs an exception and result set and aids in resource cleanup
        final boolean noOverlaps = executeQuery(query, arguments, (exception, resultSet
        ) -> {
            if (exception != null) return false;
            try {
                resultSet
                        .next();
                return resultSet
                        .getInt("COUNT(*)") == 0;
            } catch (SQLException exceptionSQL) {
                printSQLException(exceptionSQL);
                return false;
            }
        });
        // checks for over laps.
        if (!noOverlaps) {
            displayError(bundle.getString("error.overlapping"));
        }

        return noOverlaps;
    }

    /**
     * Various variable and field declarations
     */
    private final FilterController filterController = new FilterController();
    private final HashMap<Long, Contact> contactMap = new HashMap<>();
    private final ObservableList<Customer> customers;
    private final String selectQuery = "SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, " +
            "`End`, Customer_ID, User_ID, Contact_ID " +
            "FROM appointments";
    private FilterController.FilterFields currentFilter = null;

    public ApptListController(ObservableList<Customer> customers, MainController.EventEmitter eventEmitter) {
        super(new ApptConfigAssembler(Appointment.class), eventEmitter);
        ((ApptConfigAssembler) configAssembler).setContactMap(Collections.unmodifiableMap(contactMap));
        ((ApptConfigAssembler) configAssembler).setCustomers(Collections.unmodifiableList(customers));
        this.customers = customers;
        eventEmitter.addListener(MainController.Event.CustomerDeleted, this::populateTable);
    }




}