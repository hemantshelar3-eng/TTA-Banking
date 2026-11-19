package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.DashboardPage;
import com.tta.bank.pages.SendMoneyPage;
import com.tta.bank.pages.TransactionsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** TS_03 - Send money (TC_018 .. TC_022). Every test starts with a freshly registered user holding Rs 50,000. */
public class SendMoneyTests extends BaseTest {

    private static final BigDecimal OPENING = new BigDecimal("50000");
    //private static final List<String> EXPECTED_BENEFICIARIES =
      //      Arrays.asList("Aarav Sharma", "Meera Iyer", "Kabir Khan", "Ananya Rao", "Vikram Patel");

    private SendMoneyPage openSend() {
        registerNewUser();
        return new SendMoneyPage(driver).open();
    }

        
    @Test(groups = {"regression", "negative"},
          description = "TC_018 | Transfer without selecting a beneficiary")
    public void tc018_transferWithoutBeneficiary() {
        SendMoneyPage send = openSend();

        send.enterAmount("500").clickReview();

        Assert.assertTrue(send.isAlertDisplayed(), "Validation message should be displayed");
        Assert.assertEquals(send.getAlertText(), "Choose a beneficiary first");
        assertMoney(send.getBalance(), OPENING, "No amount should be debited");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_019 | Transfer with an empty amount")
    public void tc019_transferWithEmptyAmount() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().clickReview();

        Assert.assertTrue(send.isAmountErrorDisplayed(), "Amount validation message should be displayed");
        Assert.assertEquals(send.getAmountError(), "Enter an amount greater than zero");
        assertMoney(send.getBalance(), OPENING, "No amount should be debited");
    }

    @DataProvider(name = "invalidAmounts")
    public Object[][] invalidAmounts() {
        return new Object[][]{
                {"0",    "TC_025 zero"},
                {"-100", "TC_026 negative"},
                {"-0.5", "negative decimal"},
        };
    }

   

    @Test(groups = {"regression", "negative"},
          description = "TC_020 | Non-numeric amount is rejected or ignored")
    public void tc020_nonNumericAmount() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("abc");
        Assert.assertEquals(send.getAmountFieldValue(), "", "Number field should ignore letters");

        send.clickReview();
        Assert.assertTrue(send.isAmountErrorDisplayed(), "Amount error expected because nothing valid was entered");
        Assert.assertFalse(send.isConfirmDialogDisplayed(), "Confirm dialog must not open");
        assertMoney(send.getBalance(), OPENING, "Balance must not change");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_021 | Amount greater than the balance is refused")
    public void tc021_amountGreaterThanBalance() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("99999999");
        Assert.assertEquals(send.getAmountFieldValue(), "99999999", "Amount should be accepted in the field");
        send.clickReview();
        Assert.assertTrue(send.isConfirmDialogDisplayed(), "Review dialog opens, the balance check happens on confirm");
        send.clickConfirmAndSend();

        Assert.assertTrue(send.isAlertDisplayed(), "Insufficient balance message should be displayed");
        Assert.assertTrue(send.getAlertText().toLowerCase().matches(".*(insufficient|balance|enough|funds).*"),
                "Unexpected message: " + send.getAlertText());
        assertMoney(send.getBalance(), OPENING, "Balance should not change");
    }
   
    @Test(groups = {"regression"},
          description = "TC_022 | Double-clicking 'Confirm and send' debits only once")
    public void tc022_doubleClickDebitsOnce() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("500").clickReview();
        Assert.assertTrue(send.isConfirmDialogDisplayed());
        send.doubleClickConfirmAndSend();

        assertMoney(send.getBalance(), "49500", "Only one debit of Rs 500 expected");
        TransactionsPage tx = new TransactionsPage(driver).open();
        Assert.assertEquals(tx.getRowCount(), 2, "Exactly one new transaction expected (plus welcome credit)");
    }
}
