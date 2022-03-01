package Models;

/**
 * LoginUser Model class.
 *
 * @author Shadab Mustafa
 */

/**
 * a login user model object purely used for login purposes.
 */

public class LoginUser {



      //Various Getters and Setters.

    public String getLoginUserName() {
        return LoginUserName;
    }

    public void setLoginUserName(String LoginUserName) {
        this.LoginUserName = this.LoginUserName;
    }

    public String getLoginPassWord() {
        return LoginPassWord;
    }

    public void setLoginPassWord(String LoginPassWord) {
        this.LoginPassWord = LoginPassWord;
    }


    /**
     * Various variable and field declarations
     */
    private String LoginUserName;
    private String LoginPassWord;


}
