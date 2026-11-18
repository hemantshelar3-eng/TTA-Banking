package com.tta.bank.pages;

import com.tta.bank.base.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * receive.html - STUB.
 * The HTML of the Receive Money page was not shared, so no element locators are guessed here.
 * Add them (and the incoming-transfer flow) once receive.html is available, then enable TC_033.
 */
public class ReceiveMoneyPage extends BasePage {

    public ReceiveMoneyPage(WebDriver driver) { super(driver); }

    public ReceiveMoneyPage open() {
        open("receive.html");
        return this;
    }
}
