package components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;

public abstract class Page {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final WebDriver webDriver;

    /**
     * @param webDriver WebDriver base Definition
     */
    protected Page(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * @param tabName The tab Name where the view will be located
     */
    public void switchToTab(String tabName) {
        for (String windowHandle : webDriver.getWindowHandles()) {
            WebDriver windowObj = webDriver.switchTo().window(windowHandle);
            if (windowObj.getTitle().contains(tabName)) {
                LOGGER.info("Switching to Tab: {}", tabName);
                break;
            }
        }
    }

    /**
     * This will switch the view to the Last Tab opened
     */
    public void switchToLastTab() {
        ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
        WebDriver windowObj = webDriver.switchTo().window(tabs.get(tabs.size() - 1));
        LOGGER.info("Switching to Tab: {}", windowObj.getTitle());
    }

    /**
     * @param mainTabName     The Main Tab Name to return after the validation is finished
     * @param fileName        The FileName to be tracked in the Downloads Tab
     * @param timeOutDownload Is the Maximum time to wait until the file is downloaded
     * @return Returns "True" if the download is successful, in other cases will return "False"
     */
    public boolean fileDownloadedCorrectly(String mainTabName, String fileName, int timeOutDownload) {
        try {
            Thread.sleep(1000);
            ((JavascriptExecutor) webDriver).executeScript("window.open();");
            Thread.sleep(1000);
            switchToTab("New Tab");
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error("File Download Error: {}", ex.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } finally {
            switchToTab(mainTabName);
        }
        webDriver.navigate().to("about:downloads");
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOutDownload));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//*[contains(@displayName,'" + fileName + "') and @progress='100'])[1]")));
        return true;
    }

    /**
     * @param modalXPath Is the Modal xPath to be tracked
     * @param timeOut    Is the Maximum time to wait until the Modal disappears
     */
    public void waitForModal(String modalXPath, int timeOut) {
        try {
            boolean isVisible = false;
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(modalXPath)));
            for (int i = 0; i < timeOut; i++) {
                isVisible = !webDriver.findElements(By.xpath(modalXPath)).isEmpty();
                if (isVisible) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }
            if (isVisible) {
                throw new InterruptedException("Modal TimeOut / Still Visible");
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Modal not found.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @param timeOutLoad Time in seconds to wait until Page Loads
     */
    public void waitForPageLoad(int timeOutLoad) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeOutLoad));
        ExpectedCondition<Boolean> pageLoadCondition = driver -> {
            assert driver != null;
            return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
        };
        wait.until(pageLoadCondition);
    }
}