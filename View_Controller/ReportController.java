package View_Controller;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import Models.Division;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Report tab controller, also formats data.
 *  @author Shadab Mustafa
 */
public class ReportController extends MiscLambdaController {

    /**
     * Various variables and fields declared.
     */
    @FXML
    private TextArea textArea;


    /**
     * Report one action
     */
    @FXML
    public void handleReportOne() {
        textArea.clear();
        String reportOne;
        reportOne = reportOneText();
        textArea.setText(reportOne);

    }

    /**
     * Report two action
     */
    @FXML
    public void handleReportTwo() {
        textArea.clear();
        String reportTwo;
        reportTwo = reportTwoText();
        textArea.setText(reportTwo);

    }

    /**
     * Report three action
     */
    @FXML
    public void handleReportThree() {
        textArea.clear();
        String reportThree;
        reportThree = reportThreeText();
        textArea.setText(reportThree);

    }

    /**
     * Report two action
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


    /**
     * Filters the number of appointments by month into a human-readable format
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseMonthsCount(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            while (rs.next()) {
                final String month;
                month = bundle.getString(String.format("month.%d", rs.getInt(1)));
                output.append(String.format("\t%s:\t%d\n", month, rs.getInt(2)));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
        return output.toString();
    }

    /**
     * Filters the number of appointments by type into a human-readable format
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseTypesCount(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        String output = "";
        try {
            while (rs.next()) {
                output += String.format("\t%s:\t%d\n", rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
        return output;
    }


    /**
     * Filters the appointments and customers into a human-readable string for display , button 2
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseContactsAndAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            while (rs.next()) {
                long customerId = 0L;
                final Appointment appointment = new Appointment(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime(),
                        rs.getTimestamp(7).toLocalDateTime(),
                        rs.getLong(8),
                        rs.getLong(9),
                        rs.getLong(10));
                if (customerId != appointment.getCustomerId()) {
                    customerId = appointment.getCustomerId();
                    output.append("\n");
                    output.append(new Contact(rs.getLong(10), rs.getString(11), rs.getString(12)).toReportContactString());
                }
                output.append(appointment.toReportApptString());
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }

        return output.toString();
    }


    /**
     * Filters the divisions and customers into a human-readable string for display , button 3
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseCustomersAndDivisions(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            long divisionId = 0L;
            while (rs.next()) {
                final Customer customer = new Customer(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getLong(6)
                );
                if (divisionId != customer.getDivisionId()) {
                    divisionId = customer.getDivisionId();
                    output.append("\n");
                    output.append(new Division(rs.getLong(6), rs.getString(8), rs.getLong(7)).toReportDivisionString());
                }
                output.append(customer.toReportCustomerString());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return output.toString();
    }

    /**
     * runs the first report to get the total number of appointments by month and by type
     *
     * @return the string to display
     */
    private String reportOneText() {
        return bundle.getString("report.byMonth")
                + ":\n"
                + executeLambdaQuery("SELECT MONTH(`Start`) as `Month`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY MONTH(`Start`) " +
                "ORDER BY MONTH(`Start`)", this::parseMonthsCount)
                + "\n"
                + bundle.getString("report.byType")
                + ":\n"
                + executeLambdaQuery("SELECT `Type`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY `Type` " +
                "ORDER BY `Type`", this::parseTypesCount);

    }





    /**
     * runs the second report to get a schedule of appointments per contact
     * alias c is used to substitute for contacts table, alias a is used to substitute for appointments table.
     * @return the string to display
     */
    private String reportTwoText() {
        return executeLambdaQuery("SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, " +
                "Customer_ID, User_ID, d.Contact_ID, d.Contact_Name, d.Email " +
                "FROM appointments AS b " +
                "JOIN contacts AS d ON d.Contact_ID = b.Contact_ID " +
                "ORDER BY Contact_ID, `Start`", this::parseContactsAndAppointments);
    }


    /**
     * runs the thrid report to get a schedule of total appointments per customer
     * alias c is used to substitute for contacts table, alias a is used to substitute for appointments table.
     * @return the string to display
     */
    private String reportThreeText() {
        return executeLambdaQuery("SELECT customers.Customer_Name, COUNT(*) as 'Total' FROM customers JOIN appointments " +
                "ON customers.Customer_ID = appointments.Customer_ID GROUP BY Customer_Name", this::parseTotalAppointments);
    }

    /**
     * Filters the appointments and customers into a human-readable string for display , button 3
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */    private String parseTotalAppointments(SQLException ex, ResultSet rs) {

        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {

            output.append(String.format("%1$-65s %2$-65s \n", "Customer", "Total Appointments"));
            output.append(String.join("", Collections.nCopies(111, "_")));
            output.append("\n");

            while(rs.next()) {
                output.append(String.format("%1$s %2$65d \n",
                        rs.getString("Customer_Name"), rs.getInt("Total")));
            }
         //   statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return output.toString();
    }
}




