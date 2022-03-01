package Models;


/**
 * Country Model class.
 *
 * @author Shadab Mustafa
 */
public class Country extends Record {
    /**
     * Country object
     * @param  id, country id used for identification/organizational purposes     *
     * @param  country, country name in string format     *
     */
    public Country(int id, String country) {
        super(id);
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    /**
     * overrides the built-in toString() for display purposes in a ComboBox
     *
     * @return the name of the country
     */
    @Override
    public String toString() {
        return country;
    }

    /**
     * Various variable and field declarations
     */
    private final String country;


}