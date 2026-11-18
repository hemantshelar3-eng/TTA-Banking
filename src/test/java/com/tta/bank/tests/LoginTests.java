package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.DashboardPage;
import com.tta.bank.pages.LoginPage;
import com.tta.bank.pages.RegisterPage;
import com.tta.bank.utils.ConfigReader;
import com.tta.bank.utils.UserData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/** TS_01 - Sign in (TC_001 .. TC_012). Each test registers its own customer first, then signs out. */
public class LoginTests extends BaseTest {

    @Test(groups = {"regression", "negative"},
          description = "TC_001 | Sign in button without entering Email Address and Password")
    public void tc001_signInWithEmptyFields() {
        LoginPage login = openLogin();
        login.clickSignIn();

        Assert.assertTrue(login.isEmailErrorDisplayed(), "Email error should be displayed");
        Assert.assertEquals(login.getEmailError(), "Enter a valid email address");
        Assert.assertTrue(login.isPasswordErrorDisplayed(), "Password error should be displayed");
        Assert.assertEquals(login.getPasswordError(), "Password is required");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_002 | Sign in with valid credentials lands on dashboard with balance")
    public void tc002_signInWithValidCredentials() {
        UserData user = registerThenLogout();

        DashboardPage dash = openLogin().loginAs(user.email, user.password);

        Assert.assertTrue(dash.isLoaded(), "Dashboard should be displayed");
        Assert.assertTrue(dash.getBalanceText().contains("₹"), "Balance should be shown in rupees");
        Assert.assertEquals(dash.getAccountHolder(), user.name);
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_003 | Valid Email Address with invalid password")
    public void tc003_validEmailInvalidPassword() {
        UserData user = registerThenLogout();

        LoginPage login = openLogin().login(user.email, "Wrong@9999");

        Assert.assertTrue(login.isAlertDisplayed(), "Error message should be displayed");
        Assert.assertFalse(login.getAlertText().isEmpty(), "Error message should not be blank");
        assertOnLoginPage("User should stay on the sign in page");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_004 | Unregistered Email Address with valid password")
    public void tc004_unregisteredEmail() {
        LoginPage login = openLogin().login("unregister.user@ttabank.in", "Test@1234");

        Assert.assertTrue(login.isAlertDisplayed(), "Error message should be displayed");
        assertOnLoginPage("No session should be created - sign in form must still be shown");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_005 | Invalid Email Address format")
    public void tc005_invalidEmailFormat() {
        LoginPage login = openLogin().login("abc@", "Test@1234");

        Assert.assertTrue(login.isEmailErrorDisplayed(), "Email validation message should be displayed");
        Assert.assertEquals(login.getEmailError(), "Enter a valid email address");
        assertOnLoginPage("User should not leave the sign in page");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_006 | Valid Email Address and empty password")
    public void tc006_emptyPassword() {
        UserData user = registerThenLogout();

        LoginPage login = openLogin().enterEmail(user.email).clickSignIn();

        Assert.assertTrue(login.isPasswordErrorDisplayed(), "'Password is required' should be displayed");
        Assert.assertEquals(login.getPasswordError(), "Password is required");
        Assert.assertFalse(login.isEmailErrorDisplayed(), "Email is valid - no email error expected");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_007 | Password field masks the characters")
    public void tc007_passwordFieldIsMasked() {
        LoginPage login = openLogin().enterPassword("Test@1234");

        Assert.assertEquals(login.getPasswordFieldType(), "password",
                "Password input must be type=password so characters are not shown in plain text");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_008 | Password in different case must fail")
    public void tc008_passwordIsCaseSensitive() {
        UserData user = registerThenLogout();

        LoginPage login = openLogin().login(user.email, user.password.toLowerCase());

        Assert.assertTrue(login.isAlertDisplayed(), "Sign in should fail with an error message");
        assertOnLoginPage("User should stay on sign in page");
    }

    @Test(groups = {"regression"},
          description = "TC_009 | Sign in by pressing the Enter key")
    public void tc009_signInWithEnterKey() {
        UserData user = registerThenLogout();

        openLogin().enterEmail(user.email).enterPassword(user.password).pressEnterInPassword();

        Assert.assertTrue(new DashboardPage(driver).isLoaded(), "Enter key should submit the form and open the dashboard");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_010 | 'Open an account' link opens the registration page")
    public void tc010_openAnAccountLink() {
        RegisterPage register = openLogin().clickOpenAnAccount();

        Assert.assertTrue(register.isLoaded(), "Registration page should be displayed");
        Assert.assertTrue(register.currentUrl().contains("register.html"), "URL should be register.html");
    }

    @Test(groups = {"regression", "negative", "security"},
          description = "TC_011 | Sign in with SQL injection input")
    public void tc011_sqlInjectionIsRejected() {
        String payload = "' OR 1=1 --";

        // a) payload in the email field -> rejected as an invalid email
        LoginPage login = openLogin().login(payload, payload);
        Assert.assertTrue(login.isEmailErrorDisplayed(), "SQL text must be rejected as an invalid email");
        assertOnLoginPage("No sign in should happen (email field)");

        // b) payload in the password field with a real account -> authentication fails
        UserData user = registerThenLogout();
        login = openLogin().login(user.email, payload);
        Assert.assertTrue(login.isAlertDisplayed(), "Injected password must not authenticate");
        assertOnLoginPage("No sign in should happen (password field)");
    }

    @Test(groups = {"smoke", "regression", "security"},
          description = "TC_012 | Dashboard cannot be opened without signing in")
    public void tc012_dashboardRequiresLogin() {
        // fresh browser => no session
        driver.get(ConfigReader.baseUrl() + "dashboard.html");

        assertOnLoginPage("User should be redirected to the sign in page");
    }

    // ------------------------------------------------------------------------------------------
    // Known defect from the Excel sheet (TC_002 / TC_003 / TC_004 / TC_008 actual result):
    // "masking button not displayed" - there is no show/hide password control.
    // Run with:  mvn test -DsuiteXmlFile=testng-known-defects.xml
    // ------------------------------------------------------------------------------------------
    @Test(groups = {"known-defect"},
          description = "DEFECT | TC_002..008 - password show/hide (unmask) toggle should exist")
    public void defect_passwordUnmaskToggleMissing() {
        LoginPage login = openLogin().enterPassword("Test@1234");

        Assert.assertTrue(login.isPasswordToggleAvailable(),
                "Expected a show/hide password control (sheet: 'Password should be displayed in masked and Unmasked format')");
    }
}
