package com.tta.bank.listeners;

import com.tta.bank.utils.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Re-runs a failed test up to retry.count times (config.properties). Guards against one-off network blips. */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int max = ConfigReader.getInt("retry.count", 1);

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < max) {
            attempt++;
            return true;
        }
        return false;
    }
}
