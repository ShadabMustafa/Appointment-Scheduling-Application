package Models;


/**
 * User Model class.
 *
 * @author Shadab Mustafa
 */
final public class User extends Record {

    /**
     * Various variable and field declarations
     */
    final private String Username;

    /**
     * User object
     * @param id, user id
     * @param Username, user's username for login purposes
     */
    public User(long id, String Username) {
        super(id);
        this.Username = Username;
    }


     // Various Getters and Setters.

    public String getUserName() { return Username; }


    /**
     * overrides a built-in toString() for display purposes in a ComboBox
     *
     * @return the name of the user
     */
    @Override
    public String toString() {
        return Username;
    }
}