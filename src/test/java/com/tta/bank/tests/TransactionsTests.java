package com.tta.bank.tests;

import com.tta.bank.base.BaseTest;
import com.tta.bank.pages.QrPaymentPage;
import com.tta.bank.pages.SendMoneyPage;
import com.tta.bank.pages.TransactionsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

/** TS_05 - Transactions (TC_026 .. TC_027). */
public class TransactionsTests extends BaseTest {

    /** welcome credit + transfer to bens[0] (500) + transfer to bens[1] (300) + QR payment (200) */
    private List<String> seedActivity() {
        registerNewUser();
        SendMoneyPage send = new SendMoneyPage(driver).open();
        List<String> bens = send.getBeneficiaryNames();
        send.transfer(bens.get(0), "500", "Food", "Dinner");
        send.transfer(bens.get(1), "300", "Travel", "Cab");

        QrPaymentPage qr = new QrPaymentPage(driver).open();
        qr.enterAmount("200").selectCategory("Shopping").clickMarkPaid();
        // wait for the QR debit to land: 50000-500-300-200 = 49000
        for (int i = 0; i < 40 && qr.getBalance().compareTo(new BigDecimal("49000")) != 0; i++) {
            try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        return bens;
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_026 | Every transaction is listed exactly once")
    public void tc026_allTransactionsListed() {
        seedActivity();

        TransactionsPage tx = new TransactionsPage(driver).open();

        Assert.assertEquals(tx.getRowCount(), 4, "Welcome credit + 2 transfers + 1 QR payment");
        List<String> refs = tx.allRefs();
        Assert.assertEquals(new HashSet<>(refs).size(), refs.size(), "No duplicate references: " + refs);
        Assert.assertEquals(tx.getCountLabel(), 4, "'transactions shown' label should match the table");
        Assert.assertEquals(tx.getParty(0), "TTA General Store", "Newest first");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_027 | Ledger reconciles with the balance (opening + credits - debits = balance)")
    public void tc027_ledgerReconcilesWithBalance() {
        seedActivity();
        TransactionsPage tx = new TransactionsPage(driver).open();

        BigDecimal credits = tx.totalCredits();
        BigDecimal debits = tx.totalDebits();
        BigDecimal balance = tx.getBalance();

        assertMoney(credits, "50000", "Total credits (welcome bonus)");
        assertMoney(debits, "1000", "Total debits (500 + 300 + 200)");
        // The welcome bonus IS the opening balance and is listed as a credit, so credits - debits must equal the balance.
        assertMoney(credits.subtract(debits), balance, "credits - debits vs. current balance");
        assertMoney(tx.netOfVisibleRows(), balance, "Signed sum of all rows vs. current balance");
    }
}
