package View_Controller;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import Models.Record;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *Assembles an appointment form configuration list
 * @author Shadab Mustafa
 */

public class ApptConfigAssembler extends ConfigAssembler<Appointment, ApptConfigControllerController> {


    /**
     * @see ConfigAssembler#getInstance(Mode, Record, Function)
      * Returns an instance of the ConfigController for the Record
     */
    @Override
    public ApptConfigControllerController getInstance(Mode mode, Appointment record, Function<Appointment, Boolean> callback) {
        return new ApptConfigControllerController(getTitle(mode), contactMap, customers, mode, record, callback);
    }

    /**
     * establishes the contact map that is passed to every form controller instance. It block excessive sql requests.
     *
     * @param contactMap a map of contactId to contact models
     */
    public void setContactMap(Map<Long, Contact> contactMap) {
        this.contactMap = contactMap;
    }

    /**
     * forwards a list of all customers to every form controller configuration instance. It block excessive sql requests.
     *
     * @param customers a list of all customers
     */
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    /**
     * Various variable and field declarations
     */
    private Map<Long, Contact> contactMap;
    private List<Customer> customers;

    public ApptConfigAssembler(Class<Appointment> modelClass) {
        super(modelClass);
    }

}