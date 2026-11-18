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

/** TS_06 - Session handling, responsiveness and accessibility (TC_042 .. TC_046). */
public class SessionAndUiTests extends BaseTest {

    @Test(groups = {"smoke", "regression"},
          description = "TC_042 | Logout redirects to the sign in page")
    public void tc042_logout() {
        registerNewUser();

        new DashboardPage(driver).clickLogout();

        assertOnLoginPage("User should be redirected to the sign in page");
    }

    // Known defect (sheet: TC_042 actual result "After logout press back button page move to Dashboard").
    @Test(groups = {"known-defect"},
          description = "DEFECT | TC_042 | Browser Back after logout must not expose protected pages")
    public void tc042_backAfterLogoutMustNotShowDashboard() {
        registerNewUser();
        new DashboardPage(driver).clickLogout();

        driver.navigate().back();

        Assert.assertFalse(new DashboardPage(driver).isLoaded(),
                "Protected dashboard must not be accessible after logout (url=" + driver.getCurrentUrl() + ")");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_043 | Session survives a page reload")
    public void tc043_sessionPersistsAfterReload() {
        UserData user = registerNewUser();
        DashboardPage dash = new DashboardPage(driver);

        dash.refresh();

        Assert.assertTrue(dash.isLoaded(), "User should remain signed in after reload");
        Assert.assertEquals(dash.getAccountHolder(), user.name);
        Assert.assertTrue(dash.currentUrl().contains("dashboard.html"));
    }

    /*
     * Not automated: the sheet's "session should expire after a period of inactivity" (TC_043 actual result).
     * The app has no visible timeout, and waiting for one would make the suite unusably slow - keep as a manual test.
     */

    @Test(groups = {"regression", "security"},
          description = "TC_044 | Data isolation between two accounts on one browser")
    public void tc044_dataIsolationBetweenAccounts() {
        // Account A: send Rs 500
        UserData a = registerNewUser();
        SendMoneyPage send = new SendMoneyPage(driver).open();
        send.transfer(send.getFirstBeneficiaryName(), "500");
        assertMoney(send.getBalance(), "49500", "A's balance after transfer");
        send.clickLogout();

        // Account B on the SAME browser/localStorage
        UserData b = UserData.random();
        new com.tta.bank.pages.RegisterPage(driver).open().register(b);
        DashboardPage dashB = new DashboardPage(driver);
        assertMoney(dashB.getBalanceAmount(), "50000", "B must start with a clean Rs 50,000");
        Assert.assertEquals(dashB.getTransactionCount(), 1, "B must not see A's transactions");
        Assert.assertEquals(dashB.getAccountHolder(), b.name);
        dashB.clickLogout();

        // Back to A: data untouched
        DashboardPage dashA = new LoginPage(driver).open().loginAs(a.email, a.password);
        assertMoney(dashA.getBalanceAmount(), "49500", "A's balance must be unchanged");
        Assert.assertEquals(dashA.getTransactionCount(), 2, "A keeps their own history");
        TransactionsPage tx = new TransactionsPage(driver).open();
        Assert.assertEquals(tx.getRowCount(), 2);
    }

    @Test(groups = {"regression", "ui"},
          description = "TC_045 | Layout on a mobile viewport (390x844)")
    public void tc045_mobileViewport() {
        registerNewUser();
        driver.manage().window().setSize(new Dimension(390, 844));
        DashboardPage dash = new DashboardPage(driver).open();

        Assert.assertTrue(dash.isLoaded(), "Balance card should still be visible");
        Assert.assertTrue(dash.isHamburgerDisplayed(), "Hamburger menu should appear on mobile");

        long scrollWidth = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth");
        long clientWidth = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientWidth");
        Assert.assertTrue(scrollWidth <= clientWidth + 1,
                "No horizontal page scroll expected on mobile (scrollWidth=" + scrollWidth + ", clientWidth=" + clientWidth + ")");

        Assert.assertEquals(dash.hamburgerExpanded(), "false", "Menu starts collapsed");
        dash.clickHamburger();
        Assert.assertEquals(dash.hamburgerExpanded(), "true", "Hamburger should expand the menu");
        Assert.assertTrue(dash.isSidebarNavVisible(), "Navigation links should be visible after opening the menu");
    }

    @Test(groups = {"regression", "accessibility"},
          description = "TC_046 | Keyboard navigation on the sign in page: email -> password -> button")
    public void tc046_keyboardNavigationOnLogin() {
        LoginPage login = openLogin().focusEmail();
        Assert.assertEquals(login.activeElementId(), "loginEmail");

        login.pressTab();
        Assert.assertEquals(login.activeElementId(), "loginPassword", "TAB moves to password");

        login.pressTab();
        Assert.assertEquals(login.activeElementId(), "loginSubmit", "TAB moves to the Sign in button");
    }

    @Test(groups = {"regression"},
          description = "EXTRA | Sidebar navigation reaches every page")
    public void extra_sidebarNavigation() {
        registerNewUser();
        DashboardPage dash = new DashboardPage(driver);

        dash.clickNavSend();
        Assert.assertTrue(dash.currentUrl().contains("send.html"));
        dash.clickNavPayQr();
        Assert.assertTrue(dash.currentUrl().contains("pay-qr.html"));
        dash.clickNavTransactions();
        Assert.assertTrue(dash.currentUrl().contains("transactions.html"));
        dash.clickNavReceive();
        Assert.assertTrue(dash.currentUrl().contains("receive.html"));
        dash.clickNavDashboard();
        Assert.assertTrue(dash.currentUrl().contains("dashboard.html"));
    }

    @Test(groups = {"regression", "security"},
          description = "EXTRA | Every protected page redirects to sign in when there is no session")
    public void extra_protectedPagesRequireSession() {
        for (String page : new String[]{"dashboard.html", "send.html", "receive.html", "pay-qr.html", "transactions.html"}) {
            driver.get(com.tta.bank.utils.ConfigReader.baseUrl() + page);
            assertOnLoginPage(page + " must redirect to the sign in page");
        }
    }
}
