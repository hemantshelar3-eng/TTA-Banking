package com.tta.bank.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.tta.bank.base.DriverFactory;
import com.tta.bank.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.testng.IAnnotationTransformer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Extent report + screenshot on failure + automatic retry. Registered in testng.xml. */
public class TestListener implements ITestListener, IAnnotationTransformer {

    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getDescription();
        if (name == null || name.isEmpty()) {
            name = result.getMethod().getMethodName();
        }
        ExtentTest t = ExtentManager.get().createTest(name);
        t.assignCategory(result.getTestClass().getRealClass().getSimpleName());
        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            t.assignCategory(groups);
        }
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            t.info("Data: " + Arrays.toString(params));
        }
        TEST.set(t);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TEST.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = TEST.get();
        t.log(Status.FAIL, result.getThrowable());
        WebDriver driver = DriverFactory.get();
        if (driver != null) {
            try {
                t.fail("Screenshot at failure",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(ScreenshotUtil.base64(driver)).build());
                ScreenshotUtil.save(driver, result.getMethod().getMethodName());
            } catch (RuntimeException ignored) {
                t.warning("Could not capture screenshot");
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = TEST.get();
        if (t != null) {
            t.log(Status.SKIP, result.getThrowable() != null ? result.getThrowable().toString() : "Skipped");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.get().flush();
    }
}
