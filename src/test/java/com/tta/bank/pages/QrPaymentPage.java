package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

/** pay-qr.html */
public class QrPaymentPage extends BasePage {

    private static final By QR_CODE       = tid("qr-code");
    private static final By MERCHANT      = tid("qr-merchant");
    private static final By MERCHANT_UPI  = tid("qr-merchant-upi");
    private static final By AMOUNT        = tid("qr-amount");
    private static final By CATEGORY      = tid("qr-category");
    private static final By MARK_PAID     = tid("qr-mark-paid");
    private static final By AMOUNT_ERROR  = tid("qr-amount-error");
    private static final By ALERT         = tid("qr-error");

    public QrPaymentPage(WebDriver driver) { super(driver); }

    public QrPaymentPage open() {
        open("pay-qr.html");
        visible(QR_CODE);
        return this;
    }

    public boolean isQrDisplayed()          { return waitForDisplayed(QR_CODE, 5); }
    public String getQrAriaLabel()          { return visible(QR_CODE).getAttribute("aria-label"); }
    public boolean isQrImageRendered()      { return !driver.findElements(By.cssSelector("[data-testid='qr-code'] svg")).isEmpty(); }
    public String getMerchantName()         { return text(MERCHANT); }
    public String getMerchantUpi()          { return text(MERCHANT_UPI); }

    public QrPaymentPage enterAmount(String amount)    { type(AMOUNT, amount); return this; }
    public QrPaymentPage selectCategory(String c)      { new Select(visible(CATEGORY)).selectByVisibleText(c); return this; }
    public String getAmountFieldValue()                { return value(AMOUNT); }
    public QrPaymentPage clickMarkPaid()               { click(MARK_PAID); return this; }

    public QrPaymentPage doubleClickMarkPaid() {
        WebElement btn = visible(MARK_PAID);
        new Actions(driver).doubleClick(btn).perform();
        return this;
    }

    public boolean isAmountErrorDisplayed() { return waitForDisplayed(AMOUNT_ERROR, 3); }
    public String getAmountError()          { return text(AMOUNT_ERROR); }
    public boolean isAlertDisplayed()       { return waitForDisplayed(ALERT, 3); }
    public String getAlertText()            { return text(ALERT); }
}
