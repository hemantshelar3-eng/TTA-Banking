package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import com.tta.bank.utils.MoneyUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;

/** dashboard.html */
public class DashboardPage extends BasePage {

    private static final By BALANCE_AMOUNT   = tid("balance-amount");
    private static final By ACCOUNT_NUMBER   = tid("account-number");
    private static final By ACCOUNT_HOLDER   = tid("account-holder");
    private static final By STAT_SPENT       = tid("stat-spent-amount");
    private static final By STAT_TXN_COUNT   = tid("stat-txns-count");
    private static final By STAT_BEN_COUNT   = tid("stat-beneficiaries-count");
    private static final By RECENT_ROWS      = By.cssSelector("[data-testid='recent-tbody'] [data-testid='txn-row']");

    public DashboardPage(WebDriver driver) { super(driver); }

    public DashboardPage open() {
        open("dashboard.html");
        waitUntilLoaded();
        return this;
    }

    public void waitUntilLoaded() { visible(BALANCE_AMOUNT); }

    public boolean isLoaded()           { return waitForDisplayed(BALANCE_AMOUNT, 8); }
    public BigDecimal getBalanceAmount(){ return MoneyUtil.parse(text(BALANCE_AMOUNT)); }
    public String getAccountNumber()    { return text(ACCOUNT_NUMBER); }
    public String getAccountHolder()    { return text(ACCOUNT_HOLDER); }
    public BigDecimal getTotalSpent()   { return MoneyUtil.parse(text(STAT_SPENT)); }
    public int getTransactionCount()    { return Integer.parseInt(text(STAT_TXN_COUNT)); }
    public int getBeneficiaryCount()    { return Integer.parseInt(text(STAT_BEN_COUNT)); }
    public int getRecentRowCount()      { return driver.findElements(RECENT_ROWS).size(); }

    /** Spent amount shown in the expense tracker for a category, e.g. "Food". */
    public BigDecimal getExpenseFor(String category) {
        By row = By.cssSelector("[data-testid='expense-row-" + category.toLowerCase() + "'] .exp-amt");
        return MoneyUtil.parse(text(row));
    }
}
