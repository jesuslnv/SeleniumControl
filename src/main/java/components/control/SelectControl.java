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

import java.time.Duration;
import java.util.List;

public final class SelectControl extends Control {
    private static final Logger LOGGER = LogManager.getLogger();
    private int timeOut = 60;
    private long waitForClick = 0;
    private boolean autoScroll = true;

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
     * Defines the time to wait before click on element
     *
     * @param waitForClick Defines the time to wait before click on element
     */
    public void setWaitForClick(int waitForClick) {
        this.waitForClick = waitForClick;
    }

    /**
     * @param autoScroll Enables the option to auto scroll the view to the element (Default: true)
     */
    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    /**
     * Allows to select the value inside a Select Element
     *
     * @param value Is the "Value" to search inside the specified xPath Element
     */
    public void selectElement(String value) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(selector);
        }
        Select dropDown = new Select(selector);
        List<WebElement> options = dropDown.getOptions();
        for (WebElement option : options) {
            if (option.getText().equals(value)) {
                try {
                    Thread.sleep(waitForClick * 1000);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex.getMessage());
                    Thread.currentThread().interrupt();
                }
                option.click();
                break;
            }
        }
    }

    /**
     * Allows to select the specified item inside a Select Element
     *
     * @param elementToSelectXPath Is the button element displayed after clicking in the specified xPath Element
     */
    public void selectButtonElement(String elementToSelectXPath) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(selector);
        }
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
        selector.click();
        WebElement elementToSelect = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(elementToSelectXPath)));
        if (autoScroll) {
            navigateToElementLocation(elementToSelect);
        }
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
        elementToSelect.click();
    }

    /**
     * Allows to check multiple items inside a Select Element
     *
     * @param listOfElementsXPath List of xPaths Elements to be checked
     */
    public void selectCheckBox(List<String> listOfElementsXPath) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        try {
            Thread.sleep(waitForClick * 1000);
            if (autoScroll) {
                navigateToElementLocation(selector);
            }
            Actions actions = new Actions(webDriver);
            actions.moveToElement(selector).click().build().perform();
            for (String elementXPath : listOfElementsXPath) {
                //If this element is closed for any reason, it will be open again
                if (!webDriver.findElement(By.xpath(elementXPath)).isDisplayed()) {
                    ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
                    Thread.sleep(waitForClick * 1000);
                    if (autoScroll) {
                        navigateToElementLocation(selector);
                    }
                    actions = new Actions(webDriver);
                    actions.moveToElement(selector).click().build().perform();
                }
                WebElement elementToSelect = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(elementXPath)));
                Thread.sleep(waitForClick * 1000);
                if (autoScroll) {
                    navigateToElementLocation(selector);
                }
                actions = new Actions(webDriver);
                actions.moveToElement(elementToSelect).click().build().perform();
                //If checkbox is still visible I close the Combo
                if (webDriver.findElement(By.xpath(elementXPath)).isDisplayed()) {
                    ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
                    Thread.sleep(waitForClick * 1000);
                    if (autoScroll) {
                        navigateToElementLocation(selector);
                    }
                    actions = new Actions(webDriver);
                    actions.moveToElement(selector).click().build().perform();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void navigateToElementLocation(WebElement webElement) {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", webElement);
    }
}