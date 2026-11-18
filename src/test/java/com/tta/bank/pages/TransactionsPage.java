package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import com.tta.bank.utils.MoneyUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** transactions.html - ledger with search, type and category filters */
public class TransactionsPage extends BasePage {

    private static final By TABLE       = tid("tx-table");
    private static final By ROWS        = By.cssSelector("[data-testid='tx-tbody'] [data-testid='txn-row']");
    private static final By SEARCH      = tid("tx-search");
    private static final By TYPE_FILTER = tid("tx-type-filter");
    private static final By CAT_FILTER  = tid("tx-category-filter");
    private static final By RESET       = tid("tx-reset-filters");
    private static final By COUNT_LABEL = tid("tx-count");

    // column indexes inside a row: 0 Ref | 1 Date | 2 Party | 3 Note | 4 Category | 5 Type | 6 Amount
    private static final int COL_REF = 0, COL_DATE = 1, COL_PARTY = 2, COL_NOTE = 3, COL_CAT = 4, COL_TYPE = 5, COL_AMOUNT = 6;

    public TransactionsPage(WebDriver driver) { super(driver); }

    public TransactionsPage open() {
        open("transactions.html");
        visible(TABLE);
        visible(ROWS);
        return this;
    }

    // ---- filters ----
    public TransactionsPage filterByType(String optionValue) {
        new Select(visible(TYPE_FILTER)).selectByValue(optionValue); // all | debit | credit
        return this;
    }

    public TransactionsPage filterByCategory(String category) {
        new Select(visible(CAT_FILTER)).selectByVisibleText(category);
        return this;
    }

    public TransactionsPage search(String keyword) {
        typeWithKeyboardClear(SEARCH, keyword);
        return this;
    }

    public TransactionsPage clearSearch() {
        typeWithKeyboardClear(SEARCH, "");
        return this;
    }

    public TransactionsPage clickReset() {
        click(RESET);
        return this;
    }

    // ---- rows ----
    public List<WebElement> rows() {
        return driver.findElements(ROWS);
    }

    public int getRowCount() {
        return rows().size();
    }

    public int getCountLabel() {
        return Integer.parseInt(text(COUNT_LABEL));
    }

    private String cell(WebElement row, int col) {
        return row.findElements(By.tagName("td")).get(col).getText().trim();
    }

    public String getRef(int rowIndex)    { return cell(rows().get(rowIndex), COL_REF); }
    public String getDate(int rowIndex)   { return cell(rows().get(rowIndex), COL_DATE); }
    public String getParty(int rowIndex)  { return cell(rows().get(rowIndex), COL_PARTY); }
    public String getNote(int rowIndex)   { return cell(rows().get(rowIndex), COL_NOTE); }
    public String getCategory(int rowIndex) { return cell(rows().get(rowIndex), COL_CAT); }
    public String getType(int rowIndex)   { return cell(rows().get(rowIndex), COL_TYPE); }
    public String getAmountText(int rowIndex) { return cell(rows().get(rowIndex), COL_AMOUNT); }
    public BigDecimal getSignedAmount(int rowIndex) { return MoneyUtil.parse(getAmountText(rowIndex)); }

    public List<String> allRefs() {
        return rows().stream().map(r -> cell(r, COL_REF)).collect(Collectors.toList());
    }

    public List<String> allTypes() {
        return rows().stream().map(r -> r.getAttribute("data-type")).collect(Collectors.toList());
    }

    public List<String> allAmountTexts() {
        return rows().stream().map(r -> cell(r, COL_AMOUNT)).collect(Collectors.toList());
    }

    public List<String> allRowTexts() {
        return rows().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /** Sum of every visible signed amount (credits positive, debits negative). */
    public BigDecimal netOfVisibleRows() {
        BigDecimal sum = BigDecimal.ZERO;
        for (WebElement r : rows()) {
            sum = sum.add(MoneyUtil.parse(cell(r, COL_AMOUNT)));
        }
        return sum;
    }

    public BigDecimal totalCredits() {
        BigDecimal sum = BigDecimal.ZERO;
        for (WebElement r : rows()) {
            if ("credit".equals(r.getAttribute("data-type"))) sum = sum.add(MoneyUtil.parse(cell(r, COL_AMOUNT)));
        }
        return sum;
    }

    public BigDecimal totalDebits() {
        BigDecimal sum = BigDecimal.ZERO;
        for (WebElement r : rows()) {
            if ("debit".equals(r.getAttribute("data-type"))) sum = sum.add(MoneyUtil.parse(cell(r, COL_AMOUNT)).abs());
        }
        return sum;
    }
}
