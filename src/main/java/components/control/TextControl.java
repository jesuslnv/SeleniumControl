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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class TextControl extends Control {
    private static final Logger LOGGER = LogManager.getLogger();
    private int timeOut = 60;
    private long waitAfterClean = 0;
    private long waitForClick = 0;
    private boolean cleanText = true;
    private boolean autoScroll = true;

    /**
     * @param webDriver WebDriver base Definition
     * @param xPath     Base Component xPath to use
     */
    public TextControl(WebDriver webDriver, String xPath) {
        super(webDriver, xPath);
    }

    /**
     * @param webDriver   WebDriver base Definition
     * @param xPath       Base Component xPath to use
     * @param condTimeOut TimeOut to modify (Base is 60 seconds)
     */
    public TextControl(WebDriver webDriver, String xPath, int condTimeOut) {
        super(webDriver, xPath);
        this.timeOut = condTimeOut;
    }

    /**
     * @param waitAfterClean Defines the time to wait after clean a Text
     */
    public void setWaitAfterClean(int waitAfterClean) {
        this.waitAfterClean = waitAfterClean;
    }

    /**
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
     * @param cleanText Defines if the text will be cleaned
     */
    public void setCleanText(boolean cleanText) {
        this.cleanText = cleanText;
    }

    /**
     * @param value Is the text value to be written
     */
    public void setText(String value) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement inputText = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(inputText);
        }
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
        if (cleanText) {
            inputText.clear();
        }
        try {
            Thread.sleep(waitAfterClean * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error("Error in \"SetText\" Function: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
        inputText.sendKeys(value);
    }

    /**
     * @param value             is the Text to be written in the predefined xPath Element
     * @param autoCompleteXpath is the xPath for the displayed popup text to be clicked
     */
    public void setTextAutoComplete(String value, String autoCompleteXpath) {
        //Using the base SetText
        setText(value);
        //Adding an option to Click on the displayed autocomplete popup when you write the text
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement inputAutoComplete = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(autoCompleteXpath)));
        Actions actions = new Actions(webDriver);
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error("Error in \"SetTextAutoComplete\" Function: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
        actions.moveToElement(inputAutoComplete).moveToElement(inputAutoComplete).click().build().perform();
    }

    /**
     * @return Returns the text contained by the specified xPath Element
     */
    public String getContainedText() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOut));
        WebElement inputUser = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        return inputUser.getText();
    }

    private void navigateToElementLocation(WebElement webElement) {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", webElement);
    }
}