package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.DashboardPage;
import com.tta.bank.pages.LoginPage;
import com.tta.bank.pages.SendMoneyPage;
import com.tta.bank.pages.TransactionsPage;
import com.tta.bank.utils.UserData;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/** TS_06 - Session handling, responsiveness and accessibility (TC_028 .. TC_030). */
public class SessionAndUiTests extends BaseTest {

    @Test(groups = {"smoke", "regression"},
          description = "TC_028 | Logout redirects to the sign in page")
    public void tc028_logout() {
        registerNewUser();

        new DashboardPage(driver).clickLogout();

        assertOnLoginPage("User should be redirected to the sign in page");
    }

    // Known defect (sheet: TC_042 actual result "After logout press back button page move to Dashboard").
    @Test(groups = {"smoke", "regression"},
          description = "DEFECT | TC_029 | Browser Back after logout must not expose protected pages")
    public void tc029_backAfterLogoutMustNotShowDashboard() {
        registerNewUser();
        new DashboardPage(driver).clickLogout();

        driver.navigate().back();
Assert.assertEquals(driver.getCurrentUrl(), "https://app.thetestingacademy.com/playwright/tta-bank/", "After logout, browser back must not show dashboard");
       // Assert.assertFalse(new DashboardPage(driver).isLoaded(),
          //      "Protected dashboard must not be accessible after logout (url=" + driver.getCurrentUrl() + ")");
    }

 

    
    @Test(groups = {"regression", "accessibility"},
          description = "TC_030 | Keyboard navigation on the sign in page: email -> password -> button")
    public void tc030_keyboardNavigationOnLogin() {
        LoginPage login = openLogin().focusEmail();
        Assert.assertEquals(login.activeElementId(), "loginEmail");

        login.pressTab();
        Assert.assertEquals(login.activeElementId(), "loginPassword", "TAB moves to password");

        login.pressTab();
        Assert.assertEquals(login.activeElementId(), "loginSubmit", "TAB moves to the Sign in button");
    }

}