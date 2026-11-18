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

/** TS_05 - Transactions (TC_038 .. TC_041). */
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
          description = "TC_038 | Every transaction is listed exactly once")
    public void tc038_allTransactionsListed() {
        seedActivity();

        TransactionsPage tx = new TransactionsPage(driver).open();

        Assert.assertEquals(tx.getRowCount(), 4, "Welcome credit + 2 transfers + 1 QR payment");
        List<String> refs = tx.allRefs();
        Assert.assertEquals(new HashSet<>(refs).size(), refs.size(), "No duplicate references: " + refs);
        Assert.assertEquals(tx.getCountLabel(), 4, "'transactions shown' label should match the table");
        Assert.assertEquals(tx.getParty(0), "TTA General Store", "Newest first");
    }

    @Test(groups = {"regression"},
          description = "TC_039 | Filter transactions by type (debit / credit)")
    public void tc039_filterByType() {
        seedActivity();
        TransactionsPage tx = new TransactionsPage(driver).open();

        tx.filterByType("debit");
        Assert.assertEquals(tx.getRowCount(), 3, "3 debits expected");
        Assert.assertTrue(tx.allTypes().stream().allMatch("debit"::equals), "Only debit rows: " + tx.allTypes());
        Assert.assertTrue(tx.allAmountTexts().stream().allMatch(a -> a.startsWith("-")),
                "Every debit amount starts with a minus: " + tx.allAmountTexts());

        tx.filterByType("credit");
        Assert.assertEquals(tx.getRowCount(), 1, "1 credit (welcome bonus) expected");
        Assert.assertTrue(tx.allTypes().stream().allMatch("credit"::equals), "Only credit rows");
        Assert.assertTrue(tx.allAmountTexts().stream().allMatch(a -> a.startsWith("+")),
                "Every credit amount starts with a plus: " + tx.allAmountTexts());
        Assert.assertEquals(tx.getCountLabel(), 1);

        tx.filterByType("all");
        Assert.assertEquals(tx.getRowCount(), 4, "'All types' restores the full list");
    }

    @Test(groups = {"regression"},
          description = "TC_040 | Search by beneficiary name, then clear the filter")
    public void tc040_searchByKeyword() {
        List<String> bens = seedActivity();
        String name = bens.get(0);
        TransactionsPage tx = new TransactionsPage(driver).open();

        tx.search(name);
        Assert.assertEquals(tx.getRowCount(), 1, "Only the transfer to " + name + " should match");
        Assert.assertTrue(tx.allRowTexts().stream().allMatch(t -> t.contains(name)), "All rows match '" + name + "'");
        Assert.assertEquals(tx.getCountLabel(), 1, "Count label should update");

        tx.search("Dinner"); // notes are searchable too
        Assert.assertEquals(tx.getRowCount(), 1, "Note search should match one row");

        tx.search("zzz-no-match");
        Assert.assertEquals(tx.getRowCount(), 0, "Nonsense keyword matches nothing");

        tx.clearSearch();
        Assert.assertEquals(tx.getRowCount(), 4, "Full list should be displayed after clearing");
    }

    @Test(groups = {"regression"},
          description = "EXTRA | Category filter and Reset button")
    public void extra_categoryFilterAndReset() {
        seedActivity();
        TransactionsPage tx = new TransactionsPage(driver).open();

        tx.filterByCategory("Travel");
        Assert.assertEquals(tx.getRowCount(), 1, "One Travel transaction");

        tx.filterByType("credit").search("x");
        tx.clickReset();
        Assert.assertEquals(tx.getRowCount(), 4, "Reset restores everything");
    }

    @Test(groups = {"smoke", "regression"},
          description = "TC_041 | Ledger reconciles with the balance (opening + credits - debits = balance)")
    public void tc041_ledgerReconcilesWithBalance() {
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
