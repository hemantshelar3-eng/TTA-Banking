package com.tta.bank.base;

import com.tta.bank.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/** One WebDriver per thread, so TestNG can run classes in parallel safely. Selenium Manager downloads the driver binary. */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() { }

    public static WebDriver init() {
        String browser = ConfigReader.get("browser", "chrome").toLowerCase();
        boolean headless = ConfigReader.getBool("headless", false);
        WebDriver driver;

        switch (browser) {
            case "firefox": {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                driver = new FirefoxDriver(o);
                break;
            }
            case "edge": {
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1440,900");
                driver = new EdgeDriver(o);
                break;
            }
            case "chrome":
            default: {
                ChromeOptions o = new ChromeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1440,900", "--disable-notifications", "--remote-allow-origins=*");
                driver = new ChromeDriver(o);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        if (!headless) driver.manage().window().maximize();
        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver get() {
        return DRIVER.get();
    }

    public static void quit() {
        WebDriver d = DRIVER.get();
        if (d != null) {
            try { d.quit(); } finally { DRIVER.remove(); }
        }
    }
}
