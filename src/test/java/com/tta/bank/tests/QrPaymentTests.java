package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.QrPaymentPage;
import com.tta.bank.pages.TransactionsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/** TS_04 - QR payment (TC_023 .. TC_025). */
public class QrPaymentTests extends BaseTest {

    private static final BigDecimal OPENING = new BigDecimal("50000");

    private QrPaymentPage openQr() {
        registerNewUser();
        return new QrPaymentPage(driver).open();
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_023 | Dummy QR code and merchant payment details are displayed")
    public void tc023_qrCodeIsDisplayed() {
        QrPaymentPage qr = openQr();

        Assert.assertTrue(qr.isQrDisplayed(), "QR code should be displayed");
        Assert.assertTrue(qr.isQrImageRendered(), "QR graphic (svg) should be rendered");
        Assert.assertEquals(qr.getQrAriaLabel(), "Dummy QR code for TTA General Store");
        Assert.assertEquals(qr.getMerchantName(), "TTA General Store");
        Assert.assertEquals(qr.getMerchantUpi(), "ttageneralstore@ttabank");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_024 | Mark as paid debits the balance and logs a transaction")
    public void tc024_markAsPaidFlow() {
        QrPaymentPage qr = openQr();

        qr.enterAmount("249").selectCategory("Shopping").clickMarkPaid();

        // balance drops once the payment is processed
        waitUntil(() -> qr.getBalance().compareTo(OPENING) < 0);
        assertMoney(qr.getBalance(), "49751", "Balance should reduce by Rs 249");

        TransactionsPage tx = new TransactionsPage(driver).open();
        Assert.assertEquals(tx.getRowCount(), 2, "A new transaction should be displayed");
        Assert.assertEquals(tx.getParty(0), "TTA General Store");
        Assert.assertEquals(tx.getNote(0), "QR payment");
        Assert.assertEquals(tx.getCategory(0), "Shopping");
        Assert.assertEquals(tx.getType(0), "debit");
        assertMoney(tx.getSignedAmount(0), "-249", "Ledger amount");
    }

    @DataProvider(name = "invalidQrAmounts")
    public Object[][] invalidQrAmounts() {
        return new Object[][]{{"0"}, {"-50"}, {""}};
    }

    @Test(dataProvider = "invalidQrAmounts", groups = {"regression", "negative"},
          description = "TC_025 | QR payment with an invalid amount (0 / -50 / empty)")
    public void tc025_invalidQrAmount(String amount) {
        QrPaymentPage qr = openQr();

        qr.enterAmount(amount).clickMarkPaid();

        Assert.assertTrue(qr.isAmountErrorDisplayed(), "Invalid amount '" + amount + "' should be rejected with a message");
        Assert.assertEquals(qr.getAmountError(), "Enter an amount greater than zero");
        assertMoney(qr.getBalance(), OPENING, "Balance must not change");
    }

    /** Tiny polling helper (up to 10 s) so assertions don't race the async payment promise. */
    private void waitUntil(java.util.function.BooleanSupplier condition) {
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end) {
            try {
                if (condition.getAsBoolean()) return;
            } catch (RuntimeException ignored) { /* element not ready yet */ }
            try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }
}
