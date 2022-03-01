package Models;


/**
 * Division Model class.
 *
 * @author Shadab Mustafa
 */
public class Division extends Record {

    /**
     * Division object
     *  @param  id, Division id used for organizational/identification purposes
     *  @param  division, Division division, used to know more specific location than country
     *  @param  countryId, Division country id, general location id
     */
    public Division(long id, String division, long countryId) {
        super(id);
        this.division = division;
        this.countryId = countryId;
    }


     // Various Getters and Setters.
    public String getDivision() {
        return division;
    }

    public long getCountryId() {
        return countryId;
    }

    /**
     * overrides the built-in toString() for display purposes in a ComboBox
     *
     * @return the name of the division
     */
    @Override
    public String toString() {
        return division;
    }

    /**
     * @return a Division string that can be displayed in a report
     */
    public String toReportDivisionString() {
        return String.format("%d\t%s:\n", id, division);
    }


    /**
     * Various variable and field declarations
     */
    private final String division;
    private final long countryId;

}