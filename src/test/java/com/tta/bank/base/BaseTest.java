package com.tta.bank.base;

import com.tta.bank.pages.DashboardPage;
import com.tta.bank.pages.LoginPage;
import com.tta.bank.pages.RegisterPage;
import com.tta.bank.utils.UserData;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.math.BigDecimal;

/**
 * Every test gets a brand-new browser (and therefore empty localStorage - the app keeps all data in the browser),
 * so tests are fully independent and safe to run in parallel.
 */
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.init();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quit();
    }

    // ---------- shared fixtures ----------

    /** Registers a new random user through the UI. Ends on the dashboard, signed in, balance Rs 50,000. */
    protected UserData registerNewUser() {
        UserData user = UserData.random();
        new RegisterPage(driver).open().register(user);
        return user;
    }

    /** Registers a new user, logs out, and leaves the browser on the sign-in page. */
    protected UserData registerThenLogout() {
        UserData user = registerNewUser();
        new DashboardPage(driver).clickLogout();
        return user;
    }

    protected LoginPage openLogin() {
        return new LoginPage(driver).open();
    }

    // ---------- assertion helpers ----------

    protected static void assertMoney(BigDecimal actual, String expected, String message) {
        assertMoney(actual, new BigDecimal(expected), message);
    }

    protected static void assertMoney(BigDecimal actual, BigDecimal expected, String message) {
        Assert.assertEquals(actual.compareTo(expected), 0,
                message + " | expected=" + expected.toPlainString() + " actual=" + actual.toPlainString());
    }

    protected void assertOnLoginPage(String message) {
        Assert.assertTrue(new LoginPage(driver).isLoaded(), message + " | url=" + driver.getCurrentUrl());
    }
}
