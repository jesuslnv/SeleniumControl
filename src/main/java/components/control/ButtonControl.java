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

public final class ButtonControl extends Control {
    private static final Logger LOGGER = LogManager.getLogger();
    private int timeOut = 60;
    private long waitForClick = 0;
    private boolean autoScroll = true;

    /**
     * @param waitForClick Defines the time to wait before click on element
     */
    public void setWaitForClick(int waitForClick) {
        this.waitForClick = waitForClick;
    }

    /**
     * @param autoScroll Defines the time to wait before start actions in a function
     */
    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    /**
     * @param webDriver WebDriver base Definition
     * @param xPath     Base Component xPath to use
     */
    public ButtonControl(WebDriver webDriver, String xPath) {
        super(webDriver, xPath);
    }

    /**
     * @param webDriver   WebDriver base Definition
     * @param xPath       Base Component xPath to use
     * @param condTimeOut TimeOut to modify (Base is 60 seconds)
     */
    public ButtonControl(WebDriver webDriver, String xPath, int condTimeOut) {
        super(webDriver, xPath);
        this.timeOut = condTimeOut;
    }

    /**
     * Allows to "Click" on specified xPath Element
     */
    public void click() {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(btn);
        }
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
        btn.click();
    }

    /**
     * Allows to "Right Click" on specified xPath Element
     */
    public void rightClick() {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(btn);
        }
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
        Actions action = new Actions(webDriver).contextClick(btn);
        action.build().perform();
    }

    /**
     * Allows to "Double Click" on specified xPath Element
     */
    public void doubleClick() {
        WebDriverWait wait = new WebDriverWait(webDriver, timeOut);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        if (autoScroll) {
            navigateToElementLocation(btn);
        }
        try {
            Thread.sleep(waitForClick * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
        Actions action = new Actions(webDriver).doubleClick(btn);
        action.build().perform();
    }

    private void navigateToElementLocation(WebElement webElement) {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", webElement);
        //Element to Move XY ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
    }
}