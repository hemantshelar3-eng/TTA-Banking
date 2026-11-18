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

/** TS_03 - Send money (TC_020 .. TC_033). Every test starts with a freshly registered user holding Rs 50,000. */
public class SendMoneyTests extends BaseTest {

    private static final BigDecimal OPENING = new BigDecimal("50000");
    private static final List<String> EXPECTED_BENEFICIARIES =
            Arrays.asList("Aarav Sharma", "Meera Iyer", "Kabir Khan", "Ananya Rao", "Vikram Patel");

    private SendMoneyPage openSend() {
        registerNewUser();
        return new SendMoneyPage(driver).open();
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_020 | Saved beneficiaries list shows the five payees")
    public void tc020_savedBeneficiariesList() {
        SendMoneyPage send = openSend();

        Assert.assertEquals(send.getBeneficiaryCount(), 5, "Five saved beneficiaries expected");
        Assert.assertEquals(send.getBeneficiaryNames(), EXPECTED_BENEFICIARIES);
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_021 | Successful transfer of Rs 500 reduces the balance by Rs 500")
    public void tc021_successfulTransfer() {
        SendMoneyPage send = openSend();
        String ben = send.getFirstBeneficiaryName();

        send.selectBeneficiary(ben).enterAmount("500").selectCategory("Food").clickReview();

        // review dialog shows exactly what will be sent
        Assert.assertTrue(send.isConfirmDialogDisplayed(), "Confirm dialog should open");
        Assert.assertEquals(send.getConfirmTo(), ben);
        Assert.assertEquals(send.getConfirmUpi(), "aarav.sharma@ttabank");
        Assert.assertEquals(send.getConfirmAmount(), "₹500");
        Assert.assertEquals(send.getConfirmCategory(), "Food");

        send.clickConfirmAndSend();

        Assert.assertTrue(send.isSuccessToastDisplayed(), "Success message should be displayed");
        assertMoney(send.getBalance(), OPENING.subtract(new BigDecimal("500")), "Balance after sending Rs 500");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_022 | Transfer is recorded as a debit row in the transaction table")
    public void tc022_transferRecordedInLedger() {
        SendMoneyPage send = openSend();
        String ben = send.getFirstBeneficiaryName();
        send.transfer(ben, "500", "Food", "Lunch split");

        TransactionsPage tx = new TransactionsPage(driver).open();

        Assert.assertEquals(tx.getRowCount(), 2, "Welcome credit + new debit");
        Assert.assertEquals(tx.getParty(0), ben, "Newest row should be the transfer");
        Assert.assertEquals(tx.getType(0), "debit");
        Assert.assertEquals(tx.getNote(0), "Lunch split");
        Assert.assertEquals(tx.getCategory(0), "Food");
        assertMoney(tx.getSignedAmount(0), "-500", "Ledger amount");
        Assert.assertFalse(tx.getDate(0).isEmpty(), "Date should be displayed");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_023 | Transfer without selecting a beneficiary")
    public void tc023_transferWithoutBeneficiary() {
        SendMoneyPage send = openSend();

        send.enterAmount("500").clickReview();

        Assert.assertTrue(send.isAlertDisplayed(), "Validation message should be displayed");
        Assert.assertEquals(send.getAlertText(), "Choose a beneficiary first");
        assertMoney(send.getBalance(), OPENING, "No amount should be debited");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_024 | Transfer with an empty amount")
    public void tc024_transferWithEmptyAmount() {
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

    @Test(dataProvider = "invalidAmounts", groups = {"regression", "negative"},
          description = "TC_025 / TC_026 | Zero and negative amounts are rejected")
    public void tc025_026_zeroAndNegativeAmountRejected(String amount, String label) {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount(amount).clickReview();

        Assert.assertTrue(send.isAmountErrorDisplayed(), label + " should be rejected with a message");
        Assert.assertFalse(send.isConfirmDialogDisplayed(), "Confirm dialog must not open for " + label);
        assertMoney(send.getBalance(), OPENING, "Balance must not change");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_027 | Non-numeric amount is rejected or ignored")
    public void tc027_nonNumericAmount() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("abc");
        Assert.assertEquals(send.getAmountFieldValue(), "", "Number field should ignore letters");

        send.clickReview();
        Assert.assertTrue(send.isAmountErrorDisplayed(), "Amount error expected because nothing valid was entered");
        Assert.assertFalse(send.isConfirmDialogDisplayed(), "Confirm dialog must not open");
        assertMoney(send.getBalance(), OPENING, "Balance must not change");
    }

    @Test(groups = {"regression", "negative"},
          description = "TC_028 | Amount greater than the balance is refused")
    public void tc028_amountGreaterThanBalance() {
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
          description = "TC_029 | Decimal amount Rs 100.50 is debited exactly")
    public void tc029_decimalAmount() {
        SendMoneyPage send = openSend();
        send.transfer(send.getFirstBeneficiaryName(), "100.50");

        assertMoney(send.getBalance(), "49899.50", "Balance should reduce by exactly Rs 100.50");
    }

    @Test(groups = {"regression"},
          description = "TC_030 | Balance after two consecutive transfers (200 + 300)")
    public void tc030_twoConsecutiveTransfers() {
        SendMoneyPage send = openSend();
        List<String> bens = send.getBeneficiaryNames();

        send.transfer(bens.get(0), "200");
        assertMoney(send.getBalance(), "49800", "After the first transfer");
        send.transfer(bens.get(1), "300");

        assertMoney(send.getBalance(), "49500", "Opening balance minus Rs 500");
    }

    @Test(groups = {"regression"},
          description = "TC_031 | Balance and history persist after page reload")
    public void tc031_balancePersistsAfterReload() {
        SendMoneyPage send = openSend();
        send.transfer(send.getFirstBeneficiaryName(), "500");
        BigDecimal before = send.getBalance();

        send.refresh();
        Assert.assertEquals(driver.getCurrentUrl().contains("send.html"), true);
        assertMoney(send.getBalance(), before, "Balance after reload");

        TransactionsPage tx = new TransactionsPage(driver).open();
        Assert.assertEquals(tx.getRowCount(), 2, "History should be unchanged after reload");
    }

    @Test(groups = {"regression"},
          description = "TC_032 | Double-clicking 'Confirm and send' debits only once")
    public void tc032_doubleClickDebitsOnce() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("500").clickReview();
        Assert.assertTrue(send.isConfirmDialogDisplayed());
        send.doubleClickConfirmAndSend();

        assertMoney(send.getBalance(), "49500", "Only one debit of Rs 500 expected");
        TransactionsPage tx = new TransactionsPage(driver).open();
        Assert.assertEquals(tx.getRowCount(), 2, "Exactly one new transaction expected (plus welcome credit)");
    }

    @Test(groups = {"regression"},
          description = "EXTRA | Cancel in the confirm dialog does not debit")
    public void extra_cancelDoesNotDebit() {
        SendMoneyPage send = openSend();

        send.selectFirstBeneficiary().enterAmount("500").clickReview();
        Assert.assertTrue(send.isConfirmDialogDisplayed());
        send.clickCancel();

        assertMoney(send.getBalance(), OPENING, "Cancel must not debit");
    }

    @Test(groups = {"regression"},
          description = "EXTRA | Dashboard recent list, counters and expense tracker reflect a transfer")
    public void extra_dashboardReflectsTransfer() {
        SendMoneyPage send = openSend();
        send.transfer(send.getFirstBeneficiaryName(), "750", "Travel", null);

        DashboardPage dash = new DashboardPage(driver).open();

        assertMoney(dash.getBalanceAmount(), "49250", "Dashboard balance");
        assertMoney(dash.getTotalSpent(), "750", "Total spent");
        assertMoney(dash.getExpenseFor("Travel"), "750", "Travel category");
        Assert.assertEquals(dash.getTransactionCount(), 2);
        Assert.assertEquals(dash.getBeneficiaryCount(), 5);
        Assert.assertEquals(dash.getRecentRowCount(), 2);
    }

    /**
     * TC_033 | Receiving money.
     * DISABLED: receive.html was not part of the shared HTML, so there are no locators for it yet.
     * Share receive.html -> implement ReceiveMoneyPage -> remove enabled=false.
     */
    @Test(enabled = false, groups = {"regression"},
          description = "TC_033 | Receiving money increases the balance and adds a credit row")
    public void tc033_receivingMoney() {
        // registerNewUser();
        // new ReceiveMoneyPage(driver).open() ... trigger an incoming transfer ...
        // assert balance increased and a 'credit' row appears in TransactionsPage
    }
}
