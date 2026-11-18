package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** send.html - two step flow: Review transfer -> confirm dialog -> Confirm and send */
public class SendMoneyPage extends BasePage {

    private static final By BENEFICIARY_ROWS = By.cssSelector("[data-testid='beneficiary-list'] [role='option']");
    private static final By BENEFICIARY_NAMES = By.cssSelector("[data-testid='beneficiary-list'] [role='option'] .ben-meta b");
    private static final By AMOUNT        = tid("send-amount");
    private static final By CATEGORY      = tid("send-category");
    private static final By NOTE          = tid("send-note");
    private static final By REVIEW        = tid("send-submit");
    private static final By AMOUNT_ERROR  = tid("send-amount-error");
    private static final By ALERT         = tid("send-error");
    // confirm dialog
    private static final By DIALOG        = tid("confirm-dialog");
    private static final By CONFIRM_TO    = tid("confirm-to");
    private static final By CONFIRM_UPI   = tid("confirm-upi");
    private static final By CONFIRM_AMT   = tid("confirm-amount");
    private static final By CONFIRM_CAT   = tid("confirm-category");
    private static final By CONFIRM_SEND  = tid("confirm-send");
    private static final By CONFIRM_CANCEL= tid("confirm-cancel");

    public SendMoneyPage(WebDriver driver) { super(driver); }

    public SendMoneyPage open() {
        open("send.html");
        visible(tid("beneficiary-list"));
        return this;
    }

    // ---- beneficiaries ----
    public List<String> getBeneficiaryNames() {
        visible(BENEFICIARY_NAMES);
        return driver.findElements(BENEFICIARY_NAMES).stream()
                .map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    public int getBeneficiaryCount() {
        visible(BENEFICIARY_ROWS);
        return driver.findElements(BENEFICIARY_ROWS).size();
    }

    public SendMoneyPage selectBeneficiary(String name) {
        click(By.cssSelector("[data-testid^='beneficiary-ben-'][aria-label='Pay " + name + "']"));
        return this;
    }

    public SendMoneyPage selectFirstBeneficiary() {
        visible(BENEFICIARY_ROWS);
        List<WebElement> rows = driver.findElements(BENEFICIARY_ROWS);
        rows.get(0).click();
        return this;
    }

    public String getFirstBeneficiaryName() {
        return getBeneficiaryNames().get(0);
    }

    public boolean isBeneficiarySelected(String name) {
        return "true".equals(visible(
                By.cssSelector("[data-testid^='beneficiary-ben-'][aria-label='Pay " + name + "']"))
                .getAttribute("aria-selected"));
    }

    // ---- form ----
    public SendMoneyPage enterAmount(String amount) { type(AMOUNT, amount); return this; }
    public String getAmountFieldValue()             { return value(AMOUNT); }
    public SendMoneyPage selectCategory(String c)   { new Select(visible(CATEGORY)).selectByVisibleText(c); return this; }
    public SendMoneyPage enterNote(String note)     { type(NOTE, note); return this; }
    public SendMoneyPage clickReview()              { click(REVIEW); return this; }

    // ---- dialog ----
    public boolean isConfirmDialogDisplayed() { return waitForDisplayed(DIALOG, 3); }
    public String getConfirmTo()       { return text(CONFIRM_TO); }
    public String getConfirmUpi()      { return text(CONFIRM_UPI); }
    public String getConfirmAmount()   { return text(CONFIRM_AMT); }
    public String getConfirmCategory() { return text(CONFIRM_CAT); }

    public SendMoneyPage clickConfirmAndSend() {
        click(CONFIRM_SEND);
        waitForGone(DIALOG);
        return this;
    }

    /** Double-click on 'Confirm and send' to prove a transfer is only debited once. */
    public SendMoneyPage doubleClickConfirmAndSend() {
        WebElement btn = visible(CONFIRM_SEND);
        new Actions(driver).doubleClick(btn).perform();
        waitForGone(DIALOG);
        return this;
    }

    public SendMoneyPage clickCancel() {
        click(CONFIRM_CANCEL);
        waitForGone(DIALOG);
        return this;
    }

    // ---- messages ----
    public boolean isAmountErrorDisplayed() { return waitForDisplayed(AMOUNT_ERROR, 3); }
    public String getAmountError()          { return text(AMOUNT_ERROR); }
    public boolean isAlertDisplayed()       { return waitForDisplayed(ALERT, 5); }
    public String getAlertText()            { return text(ALERT); }

    /** The success toast has no stable test id, so we look for its text anywhere on the page. */
    public boolean isSuccessToastDisplayed() {
        return waitForDisplayed(By.xpath("//body//*[contains(normalize-space(text()),'Sent successfully')]"), 6);
    }

    // ---- high level flows ----
    /** Select beneficiary + amount + category, review, confirm. Waits for the dialog to close. */
    public SendMoneyPage transfer(String beneficiary, String amount, String category, String note) {
        selectBeneficiary(beneficiary);
        enterAmount(amount);
        if (category != null) selectCategory(category);
        if (note != null) enterNote(note);
        clickReview();
        isConfirmDialogDisplayed();
        clickConfirmAndSend();
        return this;
    }

    public SendMoneyPage transfer(String beneficiary, String amount) {
        return transfer(beneficiary, amount, null, null);
    }

    public List<String> categories() {
        List<String> out = new ArrayList<>();
        new Select(visible(CATEGORY)).getOptions().forEach(o -> out.add(o.getText()));
        return out;
    }
}
