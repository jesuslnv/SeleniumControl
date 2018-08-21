package components;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Control {
    protected final WebDriver webDriver;
    protected String xPath;
    protected int xPosition = 0;
    protected int yPosition = 0;

    /**
     * @param xPosition Defines the X Position to Move the View
     */
    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    /**
     * @param yPosition Defines the Y Position to Move the View
     */
    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    /**
     * @param webDriver WebDriver base Definition
     * @param xPath     Base Component xPath to use
     */
    protected Control(WebDriver webDriver, String xPath) {
        this.webDriver = webDriver;
        this.xPath = xPath;
    }

    /**
     * @return True or False if the control Exists or Not
     */
    public boolean isControlExist() {
        WebDriverWait wait = new WebDriverWait(webDriver, 60);
        WebElement webElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPath)));
        return (webElement != null);
    }

    /**
     * @param dropElementXpath Place where the base Element xPath will be dropped
     */
    public void dragAndDrop(String dropElementXpath) {
        Actions actions = new Actions(webDriver);
        WebDriverWait wait = new WebDriverWait(webDriver, 60);
        WebElement elementSource = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        WebElement elementTarget = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(dropElementXpath)));
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
        actions.dragAndDrop(elementSource, elementTarget).build().perform();
    }

    /**
     * Put the mouse Above the predefined xPath
     */
    public void mosueHover() {
        Actions actions = new Actions(webDriver);
        WebDriverWait wait = new WebDriverWait(webDriver, 60);
        WebElement elementSource = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(" + xPosition + "," + yPosition + ")", "");
        actions.moveToElement(elementSource).build().perform();
    }
}