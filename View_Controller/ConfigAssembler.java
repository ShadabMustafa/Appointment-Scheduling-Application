package View_Controller;

import Models.Record;

import java.util.function.Function;


/**
 * Used to assemble various form configurations
 * @author Shadab Mustafa
 */

/**
 * Instantiates a form configuration controller of type T for model R
 *
 * @param <R> a Record subclass
 * @param <T> a ConfigController subclass
 */
public abstract class ConfigAssembler<R extends Record, T extends ConfigController<R>> extends MiscLambdaController {

    /**
     * Various variable and field declarations
     */
    private final Class<R> modelClass;

    public ConfigAssembler(Class<R> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * Gets the appropriate string from the bundle based off the class name and the form configuration mode
     *
     * @param mode the mode the form opens in
     * @return the title for the form window
     */
    protected String getTitle(Mode mode) {
        return bundle.getString(String.format("form.%s.%s", mode.toString().toLowerCase(), modelClass.getSimpleName().toLowerCase()));
    }

    /**
     * Returns an instance of the ConfigController for the Record
     *
     * @param mode     the mode/type to open the form in
     * @param record   the record to create/read/update
     * @param callback the callback that will work on the record after an edit is complete
     * @return the form configuration controller instance
     */
    abstract public T getInstance(Mode mode, R record, Function<R, Boolean> callback);
    enum Mode {
        Create,
        Read,
        Update
    }


}