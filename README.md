# TTA Bank – Selenium + TestNG Automation Framework

UI automation for **https://app.thetestingacademy.com/playwright/tta-bank/**, built from your
`TTA_Bank_Test_Cases_Format.xlsx` (TC_001 – TC_043 + the three TS_06 scenarios TC_044 – TC_046) and the page HTML you shared.

**Stack:** Java 17 · Maven · Selenium 4.25 (Selenium Manager – no driver downloads needed) · TestNG 7.10 · ExtentReports 5 · Page Object Model

## Run

```bash
mvn clean test                                         # full regression (known defects excluded)
mvn clean test -Dheadless=true                         # headless
mvn clean test -Dbrowser=firefox                       # chrome (default) | firefox | edge
mvn clean test -DsuiteXmlFile=testng-smoke.xml         # smoke only
mvn clean test -DsuiteXmlFile=testng-known-defects.xml # the defects logged in your Excel sheet (expected to FAIL)
mvn clean test -Dtest=LoginTests                       # one class
```

Reports: `target/extent-report/index.html` (with failure screenshots) · `target/surefire-reports/` · `target/screenshots/`

## Project layout

```
pom.xml
testng.xml / testng-smoke.xml / testng-known-defects.xml
src/test/resources/config.properties          base url, browser, headless, timeouts, retry count
src/test/java/com/tta/bank/
  base/        DriverFactory (ThreadLocal driver) · BasePage (waits, shell: sidebar/top bar) · BaseTest
  pages/       LoginPage · RegisterPage · DashboardPage · SendMoneyPage · QrPaymentPage · TransactionsPage · ReceiveMoneyPage (stub)
  tests/       LoginTests · RegistrationTests · SendMoneyTests · QrPaymentTests · TransactionsTests · SessionAndUiTests
  listeners/   TestListener (Extent + screenshot + retry hookup) · ExtentManager · RetryAnalyzer
  utils/       ConfigReader · UserData (unique random customer) · MoneyUtil (₹ parsing) · ScreenshotUtil
```

## Design decisions

* **Locators** use the `data-testid` attributes in your HTML – the most stable option on this app.
* **Every test is independent.** The app stores everything in browser localStorage, so each test opens a fresh browser,
  registers a unique customer through the UI (balance ₹50,000) and works with that. No shared state → safe to run in
  parallel (`parallel="classes"`, 3 threads in `testng.xml`).
* **Money is `BigDecimal`**, parsed from the UI text (`-₹1,000` → `-1000`), so decimal checks (₹100.50) are exact.
* **Known defects** from the *Actual Result / Status* columns of your sheet are written as tests in group
  `known-defect` and excluded from the normal run so the main suite is green when the app behaves as before:

| Sheet finding | Test |
|---|---|
| No show/hide (unmask) password control (TC_002/003/004/008) | `LoginTests.defect_passwordUnmaskToggleMissing` |
| Weak password `12345678` accepted (TC_018) | `RegistrationTests.tc018_weakPasswordShouldBeRejected` |
| Back button after logout shows dashboard (TC_042) | `SessionAndUiTests.tc042_backAfterLogoutMustNotShowDashboard` |
| Session does not expire after inactivity (TC_043) | not automated – keep manual (needs a long wait) |

## Test case traceability

| Test case | Test method |
|---|---|
| TC_001 – TC_012 | `LoginTests.tc001 … tc012` |
| TC_013 – TC_017, TC_019 | `RegistrationTests.tc013 … tc019` (+ extra field-validation tests) |
| TC_020 – TC_032 | `SendMoneyTests.tc020 … tc032` (TC_025/026 share one data-driven test) |
| TC_033 (receive money) | `SendMoneyTests.tc033` – **disabled**, see below |
| TC_034 – TC_037 | `QrPaymentTests` |
| TC_038 – TC_041 | `TransactionsTests` |
| TC_042 – TC_046 | `SessionAndUiTests` |

## Things you need to know

1. **TC_033 (receive money) is not automated yet.** `receive.html` wasn't among the pages shared, so I did not guess its locators.
   Send me that HTML and I'll fill in `ReceiveMoneyPage` and enable the test.
2. **Success toast:** the toast has no `data-testid`, so the success message is matched by its text ("Sent successfully").
   Balance and ledger assertions are the primary checks.
3. **Excel test data:** your sheet uses a fixed e-mail (`softwaretestingai708@atomicmail.io`). The framework generates a
   unique e-mail per test instead (`auto.<timestamp>.<rnd>@ttabank.in`) so runs never clash on "already registered".
4. **Retry:** failed tests are retried once (`retry.count` in `config.properties`; set `0` to turn off).
5. TC_046 asserts the tab order email → password → *Sign in securely*.
