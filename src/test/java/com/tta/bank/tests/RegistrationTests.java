package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.DashboardPage;
import com.tta.bank.pages.LoginPage;
import com.tta.bank.pages.RegisterPage;
import com.tta.bank.pages.TransactionsPage;
import com.tta.bank.utils.ConfigReader;
import com.tta.bank.utils.UserData;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** TS_02 - Registration (TC_013 .. TC_019) + a few extra field-validation checks. */
public class RegistrationTests extends BaseTest {

    private RegisterPage openRegister() {
        return new RegisterPage(driver).open();
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_013 | Registration with valid details creates the account and signs in")
    public void tc013_registerWithValidDetails() {
        UserData user = UserData.random();
        RegisterPage page = openRegister();

        // password fields must be masked while typing
        page.fillForm(user);
        Assert.assertEquals(page.getPasswordFieldType(), "password", "Password should be masked");
        Assert.assertEquals(page.getConfirmFieldType(), "password", "Confirm password should be masked");
        page.clickSubmit();

        DashboardPage dash = new DashboardPage(driver);
        Assert.assertTrue(dash.isLoaded(), "Account should be created and user taken to the dashboard");
        Assert.assertEquals(dash.getAccountHolder(), user.name);

        // and the new account can sign in again
        dash.clickLogout();
        Assert.assertTrue(openLogin().loginAs(user.email, user.password).isLoaded(), "User should be able to sign in");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_014 | Opening balance of a new account is Rs 50,000")
    public void tc014_openingBalanceIs50000() {
        registerNewUser();
        DashboardPage dash = new DashboardPage(driver);

        assertMoney(dash.getBalanceAmount(), ConfigReader.get("opening.balance", "50000"), "Dashboard balance");
        assertMoney(dash.getBalance(), ConfigReader.get("opening.balance", "50000"), "Top bar balance");
        Assert.assertEquals(dash.getTransactionCount(), 1, "Only the welcome credit should exist");
        Assert.assertTrue(dash.getAccountNumber().startsWith("A/C TTA"), "Account number should be generated");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_015 | Registration without entering any details")
    public void tc015_registerWithEmptyForm() {
        RegisterPage page = openRegister().clickSubmit();

        Assert.assertTrue(page.isNameErrorDisplayed(), "Name error");
        Assert.assertEquals(page.getNameError(), "Full name must be at least 3 characters");
        Assert.assertTrue(page.isEmailErrorDisplayed(), "Email error");
        Assert.assertEquals(page.getEmailError(), "Enter a valid email address");
        Assert.assertTrue(page.isPhoneErrorDisplayed(), "Phone error");
        Assert.assertEquals(page.getPhoneError(), "Phone must be exactly 10 digits");
        Assert.assertTrue(page.isPasswordErrorDisplayed(), "Password error");
        Assert.assertEquals(page.getPasswordError(), "Password must be at least 8 characters");
        Assert.assertTrue(page.isConfirmErrorDisplayed(), "Confirm error");
        Assert.assertEquals(page.getConfirmError(), "Passwords do not match");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_016 | Registration with invalid Email Address format")
    public void tc016_registerWithInvalidEmail() {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(u.name, "user@", u.phone, u.password, u.password).clickSubmit();

        Assert.assertTrue(page.isEmailErrorDisplayed(), "Email validation message should be displayed");
        Assert.assertEquals(page.getEmailError(), "Enter a valid email address");
        Assert.assertFalse(page.isNameErrorDisplayed(), "Other fields are valid");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_017 | Registration with an already registered Email Address")
    public void tc017_registerWithDuplicateEmail() {
        UserData user = registerThenLogout();

        RegisterPage page = new RegisterPage(driver).open();
        page.fillForm(user).clickSubmit();

        Assert.assertTrue(page.isAlertDisplayed(), "A duplicate-email message should be displayed");
        Assert.assertTrue(page.getAlertText().toLowerCase().contains("already"),
                "Message should say the email is already registered, but was: " + page.getAlertText());
        Assert.assertTrue(page.currentUrl().contains("register.html"), "User should stay on registration page");
    }

    @Test(groups = {"regression"},
          description = "TC_019 | Sign in link on the registration page")
    public void tc019_signInLink() {
        LoginPage login = openRegister().clickSignInLink();

        Assert.assertTrue(login.isLoaded(), "Sign in page should be displayed");
    }

    // ---------------------------- extra field validation (not in the sheet) ----------------------------

    @DataProvider(name = "badNames")
    public Object[][] badNames() {
        return new Object[][]{{"Al"}, {" A "}, {""}};
    }

    @Test(dataProvider = "badNames", groups = {"regression", "negative"},
          description = "EXTRA | Full name shorter than 3 characters is rejected")
    public void extra_nameTooShort(String name) {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(name, u.email, u.phone, u.password, u.password).clickSubmit();

        Assert.assertTrue(page.isNameErrorDisplayed(), "Name error expected for '" + name + "'");
    }

    @DataProvider(name = "badPhones")
    public Object[][] badPhones() {
        return new Object[][]{{"12345"}, {"98765"}, {""}, {"abcdefghij"}};
    }

    @Test(dataProvider = "badPhones", groups = {"regression", "negative"},
          description = "EXTRA | Phone must be exactly 10 digits")
    public void extra_phoneMustBeTenDigits(String phone) {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(u.name, u.email, phone, u.password, u.password).clickSubmit();

        Assert.assertTrue(page.isPhoneErrorDisplayed(), "Phone error expected for '" + phone + "'");
        Assert.assertEquals(page.getPhoneError(), "Phone must be exactly 10 digits");
    }

    @Test(groups = {"regression", "negative"},
          description = "EXTRA | Password shorter than 8 characters is rejected")
    public void extra_passwordTooShort() {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(u.name, u.email, u.phone, "Ab@1", "Ab@1").clickSubmit();

        Assert.assertTrue(page.isPasswordErrorDisplayed(), "Password length error expected");
        Assert.assertEquals(page.getPasswordError(), "Password must be at least 8 characters");
    }

    @Test(groups = {"regression", "negative"},
          description = "EXTRA | Password and confirmation must match")
    public void extra_passwordMismatch() {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(u.name, u.email, u.phone, "Test@1234", "Test@4321").clickSubmit();

        Assert.assertTrue(page.isConfirmErrorDisplayed(), "Mismatch error expected");
        Assert.assertEquals(page.getConfirmError(), "Passwords do not match");
    }

    @Test(groups = {"regression"},
          description = "EXTRA | Welcome bonus appears as the first credit in the transaction table")
    public void extra_welcomeBonusInLedger() {
        registerNewUser();
        TransactionsPage tx = new TransactionsPage(driver).open();

        Assert.assertEquals(tx.getRowCount(), 1, "One welcome transaction expected");
        Assert.assertEquals(tx.getType(0), "credit");
        assertMoney(tx.getSignedAmount(0), "50000", "Welcome bonus amount");
    }

    // ------------------------------------------------------------------------------------------
    // Known defect from the Excel sheet - TC_018 "Weak password accepted".
    // The app only checks length >= 8, so "12345678" is accepted. Expected to FAIL until fixed.
    // ------------------------------------------------------------------------------------------
    @Test(groups = {"known-defect"},
          description = "DEFECT | TC_018 | Weak password (12345678) must be rejected with a password rule message")
    public void tc018_weakPasswordShouldBeRejected() {
        UserData u = UserData.random();
        RegisterPage page = openRegister().fillForm(u.name, u.email, u.phone, "12345678", "12345678").clickSubmit();

        Assert.assertTrue(page.isPasswordErrorDisplayed() || page.isAlertDisplayed(),
                "Weak password should be rejected, but the app moved on to: " + page.currentUrl());
    }
}
