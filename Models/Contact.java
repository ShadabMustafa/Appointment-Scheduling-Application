package Models;


/**
 * Contact Model class.
 *
 * @author Shadab Mustafa
 */
public class Contact extends Record {

    /**
     * Contact object
     * @param  id, Contact id, use for organization/identificaiton
     * @param  name, contact name
     * @param  email, contact email
     */
    public Contact(long id, String name, String email) {
        super(id);
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    /**
     * overrides a built-in toString() for display purposes in a ComboBox
     *
     * @return the name of the contact
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @return a Contact string that can be displayed in a report
     */
    public String toReportContactString() {
        return String.format("%d\t%s\t%s\n", id, name, email);
    }

    /**
     * Various variable and field declarations
     */
    final String name;
    final String email;


}