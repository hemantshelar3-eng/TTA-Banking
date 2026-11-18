package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import com.tta.bank.utils.UserData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** register.html - Open an account */
public class RegisterPage extends BasePage {

    private static final By FORM          = tid("register-form");
    private static final By NAME          = tid("register-name");
    private static final By EMAIL         = tid("register-email");
    private static final By PHONE         = tid("register-phone");
    private static final By PASSWORD      = tid("register-password");
    private static final By CONFIRM       = tid("register-confirm");
    private static final By SUBMIT        = tid("register-submit");
    private static final By NAME_ERR      = tid("register-name-error");
    private static final By EMAIL_ERR     = tid("register-email-error");
    private static final By PHONE_ERR     = tid("register-phone-error");
    private static final By PASSWORD_ERR  = tid("register-password-error");
    private static final By CONFIRM_ERR   = tid("register-confirm-error");
    private static final By ALERT         = tid("register-error");
    private static final By LOGIN_LINK    = tid("link-login");

    public RegisterPage(WebDriver driver) { super(driver); }

    public RegisterPage open() {
        open("register.html");
        waitUntilLoaded();
        return this;
    }

    public void waitUntilLoaded() { visible(FORM); }

    public RegisterPage enterName(String v)     { type(NAME, v); return this; }
    public RegisterPage enterEmail(String v)    { type(EMAIL, v); return this; }
    public RegisterPage enterPhone(String v)    { type(PHONE, v); return this; }
    public RegisterPage enterPassword(String v) { type(PASSWORD, v); return this; }
    public RegisterPage enterConfirm(String v)  { type(CONFIRM, v); return this; }
    public RegisterPage clickSubmit()           { click(SUBMIT); return this; }

    public RegisterPage fillForm(String name, String email, String phone, String password, String confirm) {
        enterName(name);
        enterEmail(email);
        enterPhone(phone);
        enterPassword(password);
        enterConfirm(confirm);
        return this;
    }

    public RegisterPage fillForm(UserData u) {
        return fillForm(u.name, u.email, u.phone, u.password, u.password);
    }

    /** Fill + submit. A successful registration auto-signs-in and lands on the dashboard. */
    public DashboardPage register(UserData u) {
        fillForm(u).clickSubmit();
        DashboardPage dash = new DashboardPage(driver);
        dash.waitUntilLoaded();
        return dash;
    }

    public LoginPage clickSignInLink() {
        click(LOGIN_LINK);
        LoginPage lp = new LoginPage(driver);
        lp.isLoaded();
        return lp;
    }

    // ---- state / messages ----
    public boolean isNameErrorDisplayed()     { return waitForDisplayed(NAME_ERR, 3); }
    public boolean isEmailErrorDisplayed()    { return waitForDisplayed(EMAIL_ERR, 3); }
    public boolean isPhoneErrorDisplayed()    { return waitForDisplayed(PHONE_ERR, 3); }
    public boolean isPasswordErrorDisplayed() { return waitForDisplayed(PASSWORD_ERR, 3); }
    public boolean isConfirmErrorDisplayed()  { return waitForDisplayed(CONFIRM_ERR, 3); }
    public boolean isAlertDisplayed()         { return waitForDisplayed(ALERT, 5); }

    public String getNameError()     { return text(NAME_ERR); }
    public String getEmailError()    { return text(EMAIL_ERR); }
    public String getPhoneError()    { return text(PHONE_ERR); }
    public String getPasswordError() { return text(PASSWORD_ERR); }
    public String getConfirmError()  { return text(CONFIRM_ERR); }
    public String getAlertText()     { return text(ALERT); }

    public String getPasswordFieldType() { return visible(PASSWORD).getAttribute("type"); }
    public String getConfirmFieldType()  { return visible(CONFIRM).getAttribute("type"); }
    public boolean isLoaded()            { return isDisplayed(FORM); }
}
