package components.control;

import components.Control;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public final class SelectControl extends Control {
    private static final Logger LOGGER = LogManager.getLogger();
    private int timeOut = 60;
    private int waitForClick = 0;

    /**
     * @param waitForClick Defines the time to wait before click on element
     */
    public void setWaitForClick(int waitForClick) {
        this.waitForClick = waitForClick;
    }

    /**
     * @param webDriver WebDriver base Definition
     * @param xPath     Base Component xPath to use
     */
    public SelectControl(WebDriver webDriver, String xPath) {
        super(webDriver, xPath);
    }

    /**
     * @param webDriver   WebDriver base Definition
     * @param xPath       Base Component xPath to use
     * @param condTimeOut TimeOut to modify (Base is 60 seconds)
     */
    public SelectControl(WebDriver webDriver, String xPath, int condTimeOut) {
        super(webDriver, xPath);
        this.timeOut = condTimeOut;
    }

    /**
     * @param value Is the "Value" to search inside the specified xPath component
     */
    public void selectElement(String value) {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        Select dropDown = new Select(selector);
        List<WebElement> Options = dropDown.getOptions();
        for (WebElement option : Options) {
            if (option.getText().equals(value)) {
                try {
                    Thread.sleep(waitForClick * 1000);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex.getMessage());
                }
                option.click();
                break;
            }
        }
    }

    /**
     * @param elementXPath Is the button element displayed after clicking in the specified xPath component
     */
    public void selectButtonElement(String elementXPath) {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        Actions action = new Actions(webDriver);
        action.moveToElement(selector).build().perform();
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
        }
        selector.click();
        WebElement elementToSelect = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(elementXPath)));
        // Scrolls the element to be visible
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", elementToSelect);
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
        }
        elementToSelect.click();
    }

    /**
     * @param listOfElementsXPath List of xPaths Elements to be checked
     */
    public void selectCheckBox(List<String> listOfElementsXPath) {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        try {
            Thread.sleep(waitForClick * 1000);
            Actions actions = new Actions(webDriver);
            actions.moveToElement(selector).moveToElement(selector).click().build().perform();
            for (String elementXPath : listOfElementsXPath) {
                // If this element is closed for any reason, it will be open again
                if (!webDriver.findElement(By.xpath(elementXPath)).isDisplayed()) {
                    ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
                    Thread.sleep(waitForClick * 1000);
                    actions = new Actions(webDriver);
                    actions.moveToElement(selector).moveToElement(selector).click().build().perform();
                }
                WebElement elementToSelect = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(elementXPath)));
                Thread.sleep(waitForClick * 1000);
                actions = new Actions(webDriver);
                actions.moveToElement(elementToSelect).moveToElement(elementToSelect).click().build().perform();
                // If checkbox is still visible I close the Combo
                if (webDriver.findElement(By.xpath(elementXPath)).isDisplayed()) {
                    ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
                    Thread.sleep(waitForClick * 1000);
                    actions = new Actions(webDriver);
                    actions.moveToElement(selector).moveToElement(selector).click().build().perform();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}