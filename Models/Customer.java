package Models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Model class.
 *
 * @author Shadab Mustafa
 */
public final class Customer extends Record implements Model<Customer> {

    /**
     * Customer object
     *  @param  id, Customer id, used for identification
     *  @param  name, Customer name
     *  @param  address, Customer address where customer resides
     *  @param  postalCode, Customer postal code, helped to locate customer residence
     *  @param  phone, Customer phone number, helps contact customer
     *  @param  divisionId, Customer divisionId, helps find customer's location
     */
    public Customer(long id, String name, String address, String postalCode, String phone, long divisionId) {
        super(id);
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.divisionId = divisionId;
    }



    // Various Getters and Setters.

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name.trim();
    }

    public long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(long divisionId) {
        this.divisionId = divisionId;
    }

    /**
     * @see Model#toValues()
     * duplicates fields from one instance of the record to this one
     */
    @Override
    public List<Object> toValues() {
        return new ArrayList<>(List.of(name, address, postalCode, phone, divisionId));
    }

    /**
     * @see Model#copy()
     * duplicates fields from one instance of the record to this one
     */
    @Override
    public Customer copy() {
        return new Customer(id, name, address, postalCode, phone, divisionId);
    }

    /**
     * overrides the built-in toString() for display purposes in a ComboBox
     *
     * @return the name of the customer
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @return a Customer String that can be displayed in the report
     */
    public String toReportCustomerString() {
        return String.format("\t%d\t%s\n", id, name);
    }


    /**
     * Various variable and field declarations
     */
    private final String address;
    private final String postalCode;
    private final String phone;
    private String name;
    private long divisionId;

}