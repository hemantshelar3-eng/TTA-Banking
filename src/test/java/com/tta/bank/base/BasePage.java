package com.tta.bank.base;

import com.tta.bank.utils.ConfigReader;
import com.tta.bank.utils.MoneyUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/** Common helpers + the shell (sidebar / top bar) that every logged-in page shares. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    // ---- shell locators (identical on every logged-in page) ----
    protected static final By TOPBAR_BALANCE = tid("topbar-balance");
    protected static final By NAV_USER       = tid("nav-user");
    protected static final By NAV_DASHBOARD  = tid("nav-dashboard");
    protected static final By NAV_SEND       = tid("nav-send");
    protected static final By NAV_RECEIVE    = tid("nav-receive");
    protected static final By NAV_PAY_QR     = tid("nav-pay-qr");
    protected static final By NAV_TXNS       = tid("nav-transactions");
    protected static final By NAV_LOGOUT     = tid("nav-logout");
    protected static final By HAMBURGER      = tid("bank-hamburger");

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("timeout", 15)));
    }

    /** [data-testid='x'] */
    protected static By tid(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }

    // ---------------- generic actions ----------------
    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected void click(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    protected void type(By by, String value) {
        WebElement el = visible(by);
        el.clear();
        if (value != null && !value.isEmpty()) {
            el.sendKeys(value);
        }
    }

    /** Clears via keyboard so the page's 'input' listeners fire (WebElement.clear() does not always fire them). */
    protected void typeWithKeyboardClear(By by, String value) {
        WebElement el = visible(by);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        if (value != null && !value.isEmpty()) {
            el.sendKeys(value);
        }
    }

    protected String text(By by) {
        return visible(by).getText().trim();
    }

    protected String value(By by) {
        String v = visible(by).getAttribute("value");
        return v == null ? "" : v;
    }

    protected boolean isDisplayed(By by) {
        List<WebElement> found = driver.findElements(by);
        return !found.isEmpty() && found.get(0).isDisplayed();
    }

    /** Waits up to {@code seconds} for the element to be visible; never throws. */
    protected boolean waitForDisplayed(By by, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(by));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForGone(By by) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    protected void open(String relativePath) {
        driver.get(ConfigReader.baseUrl() + relativePath);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public void refresh() {
        driver.navigate().refresh();
    }

    // ---------------- shell ----------------
    public BigDecimal getBalance() {
        return MoneyUtil.parse(text(TOPBAR_BALANCE));
    }

    public String getBalanceText() {
        return text(TOPBAR_BALANCE);
    }

    public String getSidebarUserName() {
        return text(NAV_USER);
    }

    public void clickNavDashboard()    { click(NAV_DASHBOARD); }
    public void clickNavSend()         { click(NAV_SEND); }
    public void clickNavReceive()      { click(NAV_RECEIVE); }
    public void clickNavPayQr()        { click(NAV_PAY_QR); }
    public void clickNavTransactions() { click(NAV_TXNS); }

    public void clickLogout() {
        click(NAV_LOGOUT);
        visible(tid("login-form")); // sign-in form is back
    }

    public boolean isHamburgerDisplayed() {
        return isDisplayed(HAMBURGER);
    }

    public void clickHamburger() {
        click(HAMBURGER);
    }

    public String hamburgerExpanded() {
        return visible(HAMBURGER).getAttribute("aria-expanded");
    }

    public boolean isSidebarNavVisible() {
        return isDisplayed(NAV_DASHBOARD);
    }
}
