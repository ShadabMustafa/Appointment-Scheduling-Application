package View_Controller;

import Models.Country;
import Models.Customer;
import Models.Division;
import Models.Record;

import java.util.Map;
import java.util.function.Function;


/**
 * Helps configure the  form version for the customer table list.
 * @author Shadab Mustafa
 */
public final class CustConfigAssembler extends ConfigAssembler<Customer, CustConfigControllerController> {

    public CustConfigAssembler(Class<Customer> modelClass) {
        super(modelClass);
    }

    /**
     * @see ConfigAssembler#getInstance(Mode, Record, Function)
     */
    @Override
    public CustConfigControllerController getInstance(Mode mode, Customer record, Function<Customer, Boolean> callback) {
        return new CustConfigControllerController(getTitle(mode), divisionMap, countryMap, mode, record, callback);
    }

    /**
     * sets the division map that is forwarded to every form configuration controller instance. Also blocks excessive sql requests.
     *
     * @param divisionMap a map of divisionId:division models
     */
    public void setDivisionMap(Map<Long, Division> divisionMap) {
        this.divisionMap = divisionMap;
    }

    /**
     * sets the country map that is forwarded to every form configuration controller instance. Also blocks excessive sql requests.
     *
     * @param countryMap a map of countryId:country models
     */
    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }

    private Map<Long, Division> divisionMap;
    private Map<Long, Country> countryMap;

}