package com.tta.bank.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public final class ScreenshotUtil {

    private ScreenshotUtil() { }

    public static String base64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    /** Saves a PNG under target/screenshots and returns its path (or null if saving failed). */
    public static String save(WebDriver driver, String name) {
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);
            Path file = dir.resolve(name.replaceAll("[^a-zA-Z0-9_.-]", "_") + "_" + System.currentTimeMillis() + ".png");
            Files.write(file, Base64.getDecoder().decode(base64(driver)));
            return file.toAbsolutePath().toString();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
