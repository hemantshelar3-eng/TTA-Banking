package com.tta.bank.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.tta.bank.utils.ConfigReader;

public final class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager() { }

    public static synchronized ExtentReports get() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-report/index.html");
            spark.config().setDocumentTitle("TTA Bank - Automation Report");
            spark.config().setReportName("TTA Bank UI Regression");
            spark.config().setTheme(Theme.STANDARD);

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", ConfigReader.baseUrl());
            extent.setSystemInfo("Browser", ConfigReader.get("browser", "chrome"));
            extent.setSystemInfo("Headless", String.valueOf(ConfigReader.getBool("headless", false)));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
        }
        return extent;
    }
}
