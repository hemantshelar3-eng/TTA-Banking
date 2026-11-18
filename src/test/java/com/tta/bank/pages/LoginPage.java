package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.util.List;

/** index.html - Sign in */
public class LoginPage extends BasePage {

    private static final By FORM            = tid("login-form");
    private static final By EMAIL           = tid("login-email");
    private static final By PASSWORD        = tid("login-password");
    private static final By SUBMIT          = tid("login-submit");
    private static final By EMAIL_ERROR     = tid("login-email-error");
    private static final By PASSWORD_ERROR  = tid("login-password-error");
    private static final By ALERT           = tid("login-error");
    private static final By REGISTER_LINK   = tid("link-register");
    // Not present in the current build (see TC_002 in the sheet) - used by the known-defect test.
    private static final By PASSWORD_TOGGLE = By.cssSelector(
            "[data-testid*='toggle'], [aria-label*='show password' i], [aria-label*='unmask' i], [aria-label*='hide password' i]");

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open() {
        open("index.html");
        visible(FORM);
        return this;
    }

    public LoginPage enterEmail(String email)       { type(EMAIL, email); return this; }
    public LoginPage enterPassword(String password) { type(PASSWORD, password); return this; }
    public LoginPage clickSignIn()                  { click(SUBMIT); return this; }

    public LoginPage pressEnterInPassword() {
        visible(PASSWORD).sendKeys(Keys.ENTER);
        return this;
    }

    /** Fills the form and clicks Sign in. Does not assume success. */
    public LoginPage login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        return clickSignIn();
    }

    public DashboardPage loginAs(String email, String password) {
        login(email, password);
        DashboardPage dash = new DashboardPage(driver);
        dash.waitUntilLoaded();
        return dash;
    }

    public RegisterPage clickOpenAnAccount() {
        click(REGISTER_LINK);
        RegisterPage rp = new RegisterPage(driver);
        rp.waitUntilLoaded();
        return rp;
    }

    // ---- state / messages ----
    public boolean isEmailErrorDisplayed()    { return waitForDisplayed(EMAIL_ERROR, 3); }
    public boolean isPasswordErrorDisplayed() { return waitForDisplayed(PASSWORD_ERROR, 3); }
    public boolean isAlertDisplayed()         { return waitForDisplayed(ALERT, 5); }
    public String getEmailError()             { return text(EMAIL_ERROR); }
    public String getPasswordError()          { return text(PASSWORD_ERROR); }
    public String getAlertText()              { return text(ALERT); }
    public String getPasswordFieldType()      { return visible(PASSWORD).getAttribute("type"); }
    public boolean isPasswordToggleAvailable() {
        List<?> found = driver.findElements(PASSWORD_TOGGLE);
        return !found.isEmpty();
    }
    public boolean isLoaded()                 { return isDisplayed(FORM); }

    // ---- keyboard navigation helpers ----
    public String activeElementId() {
        Object id = ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.activeElement ? document.activeElement.id : ''");
        return id == null ? "" : id.toString();
    }

    public LoginPage focusEmail() { visible(EMAIL).click(); return this; }

    public LoginPage pressTab() {
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        return this;
    }
}
