package View_Controller;

import Models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;


/**
 * Customer table list controller.
 * @author Shadab Mustafa
 */
public final class CustListController extends ListController<Customer> {

    /**
     * lambda1:  convert  division id:displayable division name
     * lambda2:  convert  country id:displayable country name
     *
     * @see ListController#addColumns()
     */
    @Override
    protected final void addColumns() {
        final TableColumn<Customer, String> nameColumn = getStringColumn(Customer.class, "name");
        final TableColumn<Customer, String> addressColumn = getStringColumn(Customer.class, "address");
        final TableColumn<Customer, String> postalCodeColumn = getStringColumn(Customer.class, "postalCode");
        final TableColumn<Customer, String> phoneColumn = getStringColumn(Customer.class, "phone");
        final TableColumn<Customer, String> divisionColumn = new TableColumn<>(bundle.getString("customer.division"));
        // lambda to convert  division id:displayable division name
        divisionColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(division.getDivision());
        });
        final TableColumn<Customer, String> countryColumn = new TableColumn<>(bundle.getString("customer.country"));
        // lambda to convert  country id:displayable country name
        countryColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(countryMap.get(division.getCountryId()).getCountry());
        });
        tableView.getColumns().addAll(nameColumn, addressColumn, postalCodeColumn, phoneColumn, divisionColumn, countryColumn);
    }

    /**
     * lambda1-3: Absorbs an exception and result set and used for Resource clean up.
     *
     * @see ListController#populateData()
     */
    @Override
    protected final void populateData() {
        executeQuery("SELECT Division_ID, Division, Country_ID FROM first_level_divisions", (ex, rs) -> {
            if (ex == null) buildDivisionMap(rs);
        });
        executeQuery("SELECT Country_ID, Country FROM countries", (ex, rs) -> {
            if (ex == null) addCountries(rs);
        });
        executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, d.Division_ID, d.Country_ID " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID;", (ex, rs) -> {
            if (ex == null) consumeResultSet(rs);
        });


        LocalDateTime now = LocalDateTime.now();
        ZoneId zid = ZoneId.systemDefault();
        ZonedDateTime Zdt = now.atZone(zid);
        LocalDateTime LocalDT1 = Zdt.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime LocalDT2 = LocalDT1.plusMinutes(15);

        executeQuery("SELECT Appointment_ID, `Start` FROM appointments " +
                "WHERE `Start` BETWEEN  '" + LocalDT1 + "' AND '" + LocalDT2 +
                "' AND User_ID = ?", toArray(userId), this::notifyOfAppointments);


    }

    /**
     * Absorbs the result of the query for appointments within the next 15 minutes. If there are no upcoming
     * appointments, it displays a message stating this. Otherwise it shows a list of all the appointments within aforementioned
     * timeframe.
     *
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing the appointment rows
     */
    private void notifyOfAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        final StringBuilder appointments = new StringBuilder();
        try {
            while (rs.next()) {
                appointments
                        .append("\n")
                        .append(bundle.getString("record.id"))
                        .append(" ")
                        .append(rs.getInt(1))
                        .append(" ")
                        .append(bundle.getString("appointment.at"))
                        .append(" ")
                        .append(Appointment.formatLocalDate(rs.getTimestamp(2).toLocalDateTime()));
            }
            final String alertBody = appointments.length() != 0
                    ? bundle.getString("appointment.upcomingAppointment") + "\n" + appointments
                    : bundle.getString("appointment.noUpcomingAppointment");
            displayAlert(bundle.getString("appointment.alertTitle"), alertBody, Alert.AlertType.INFORMATION);
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    private void buildDivisionMap(ResultSet rs) {
        try {
            while (rs.next()) {
                final Division division = new Division(rs.getInt(1), rs.getString(2), rs.getInt(3));
                divisionMap.put(division.getId(), division);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    private void addCountries(ResultSet rs) {
        try {
            while (rs.next()) {
                final Country country = new Country(rs.getInt(1), rs.getString(2));
                countryMap.put(country.getId(), country);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    /**
     * @see ListController#getNewRecord()
     */
    @Override
    protected Customer getNewRecord() {
        return new Customer(0, "", "", "", "", 0);
    }

    @Override
    protected boolean canUpdate(Customer record) {
        return true;
    }

    private void consumeResultSet(ResultSet rs) {
        try {
            while (rs.next()) {
                tableView.getItems().add(new Customer(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getInt(6)
                ));
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    /**
     * @see ListController#getInsertStatement()
     */
    @Override
    public String getInsertStatement() {
        return "INSERT INTO customers " +
                "(Customer_Name, Address, Postal_Code, Phone, Division_ID, Created_By, Last_Updated_By) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * @see ListController#getUpdateStatement()
     */
    @Override
    public String getUpdateStatement() {
        return "UPDATE customers " +
                "SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Division_ID = ?, Last_Updated_By = ?, Last_Update = NOW() " +
                "WHERE Customer_ID = ?";
    }

    /**
     * @see ListController#getDeleteStatement()
     */
    @Override
    public String getDeleteStatement() {
        return "DELETE FROM customers WHERE Customer_ID = ?";
    }

    /**
     * lambda: Absorb an exception and result set and used for Resource clean up.
     *
     * @see ListController#deleteDependencies(Record)
     */
    @Override
    protected boolean deleteDependencies(Customer record) {
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        return executeUpdate("DELETE FROM appointments WHERE Customer_ID = ?", toArray(record.getId()), (ex, updates) -> ex == null);
    }

    /**
     * lambda: Absorb an exception and result set and used for Resource clean up.
     *
     * @see ListController#getDeletedMessage(Record)
     */
    @Override
    protected String getDeletedMessage(Customer customer) {
        final String appointments = executeQuery("SELECT Appointment_ID, Type FROM appointments WHERE Customer_ID = ?",
                toArray(customer.getId()),
                this::parseAppointments);

        String message = bundle.getString("record.deleted.message")
                .replace("%{record}", bundle.getString("customer.customer"));

        if (appointments.length() != 0) {
            message += "\n\n" + bundle.getString("appointment.removed") + "\n" + appointments;
        }

        return message;
    }

    /**
     * Filters the appointments that got deleted in association with a customer record
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing the appointment rows
     * @return the string to display
     */
    private String parseAppointments(SQLException ex, ResultSet rs) {
        final StringBuilder output = new StringBuilder();
        if (ex != null) return output.toString();
        try {
            while (rs.next()) {
                output.append(String.format("%s: %d, %s: %s\n",
                        bundle.getString("record.id"),
                        rs.getInt(1),
                        bundle.getString("appointment.type"),
                        rs.getString(2)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return output.toString();
    }

    /**
     * @see ListController#emitEvent()
     */
    @Override
    protected void emitEvent() {
        eventEmitter.emit(MainController.Event.CustomerDeleted);
    }

    /**
     * Various variables and fields declared.
     */
    private final HashMap<Long, Division> divisionMap = new HashMap<>();
    private final HashMap<Long, Country> countryMap = new HashMap<>();

    public CustListController(MainController.EventEmitter eventEmitter) {
        super(new CustConfigAssembler(Customer.class), eventEmitter);
        ((CustConfigAssembler) configAssembler).setDivisionMap(Collections.unmodifiableMap(divisionMap));
        ((CustConfigAssembler) configAssembler).setCountryMap(Collections.unmodifiableMap(countryMap));
    }


}
