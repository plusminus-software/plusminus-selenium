/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.plusminus.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import software.plusminus.selenium.exception.SeleniumException;
import software.plusminus.selenium.model.Range;
import software.plusminus.selenium.model.Visibility;
import software.plusminus.selenium.model.WebTestMode;
import software.plusminus.selenium.model.WebTestOptions;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
public class Selenium {

    public static final String TIMEOUT_MESSAGE_PATTERN = "Waited for %s %s elements but was: %s total elements "
            + "(%s displayed and %s hidden)";
    
    private WebDriver driver;
    private WebTestOptions options;

    @SuppressWarnings("HiddenField")
    public void openBrowser(WebTestOptions options) {
        if (!options.allowMultipleBrowsersOpened() && isBrowserOpened()) {
            return;
        }
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            WebDriverManager.chromedriver().clearDriverCache().setup();
        }
        ChromeOptions chromeOptions = new ChromeOptions();
        if (options.headlessBrowser()) {
            chromeOptions.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200",
                    "--ignore-certificate-errors", "--silent");    
        }
        driver = new ChromeDriver(chromeOptions);
    }
    
    public void closeBrowser() {
        driver.quit();
    }

    public boolean isBrowserOpened() {
        if (driver == null) {
            return false;
        }
        return !driver.toString().contains("null");
    }
    
    public WebDriver driver() {
        return driver;
    }

    @SuppressWarnings("HiddenField")
    public void loadPage(WebTestOptions options, String pathOrUrl) {
        this.options = options;
        String url = buildUrl(pathOrUrl); 
        if (driver.getCurrentUrl().equals(url) && !options.reloadPageOnEachTest()) {
            return;
        }
        options.beforePageLoads().run();
        driver.get(url);
        options.afterPageLoads().run();
        if (options.hideBrowser()) {
            hideBrowser();
        }
        if (options.mode() == WebTestMode.DESKTOP) {
            desktopWindow();
        } else if (options.mode() == WebTestMode.MOBILE) {
            mobileWindow();
        }
        checkErrorsInLogs();
    }

    public List<WebElement> findAll(SearchContext context, By by, Range size, Visibility visibility) {
        waitForPage();
        waitForElement(context, by, size, visibility);
        waitForElement(context, by, size, visibility);
        List<WebElement> elements = findElements(context, by, visibility);
        if (!size.between(elements.size())) {
            throw new SeleniumException(getErrorMessage(size, visibility, elements));
        }
        return elements;
    }

    public void moveToElement(WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element.getCanonicalElement())
                .build().perform();
    }

    public void dragAndDrop(WebElement from, WebElement to) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(from.getCanonicalElement(), to.getCanonicalElement())
                .build().perform();
    }

    public void checkErrorsInLogs() {
        LogEntries logEntries = driver.manage().logs()
                .get(LogType.BROWSER);
        List<LogEntry> logs = logEntries.getAll().stream()
                .filter(options.logsFilter())
                .collect(Collectors.toList());
        if (!logs.isEmpty()) {
            throw new AssertionError("expected: <no errors in logs> but was: <" + logs + ">");
        }
    }

    public String buildUrl(String path) {
        if (path.contains("://")) {
            return path;
        }
        return String.format("%s://%s:%s%s",
                options.protocol(), options.host(), options.port(), path);
    }
    
    public void go(String path) {
        driver.get(buildUrl(path));
    }

    public void desktopWindow() {
        WebDriver.Window window = driver.manage().window();
        Dimension dimension = window.getSize();
        window.setSize(new Dimension(dimension.getHeight() * 16 / 9, dimension.getHeight()));
    }

    public void mobileWindow() {
        WebDriver.Window window = driver.manage().window();
        Dimension dimension = window.getSize();
        int height = dimension.getHeight();
        int width = height * 9 / 16;
        if (width > 500) {
            width = 500;
        }
        window.setSize(new Dimension(width, height));
    }

    public void hideBrowser() {
        driver.manage().window().setPosition(new Point(-2000, 0));
    }

    public void waitForPage() {
        new WebDriverWait(driver, options.timeoutInSeconds())
                .until((ExpectedCondition<Boolean>) wd ->
                        ((JavascriptExecutor) wd)
                                .executeScript("return document.readyState")
                                .equals("complete"));
    }

    public void waitForElement(SearchContext context, By by, Range size, Visibility visibility) {
        try {
            new WebDriverWait(driver, options.timeoutInSeconds()).until(d -> {
                int actualSize = findElements(context, by, visibility).size();
                return size.between(actualSize);
            });
        } catch (TimeoutException e) {
            List<WebElement> elements = findElements(context, by, Visibility.ALL);
            throw new TimeoutException(getErrorMessage(size, visibility, elements), e);
        }
    }
    
    private List<WebElement> findElements(SearchContext context, By by, Visibility visibility) {
        return context.findElements(by).stream()
                .map(e -> WebElement.of(e, this))
                .filter(e -> filterByVisibility(e, visibility))
                .collect(Collectors.toList());
    }

    private boolean filterByVisibility(WebElement element, Visibility visibility) {
        if (visibility == Visibility.DISPLAYED) {
            return element.isDisplayed();
        } else if (visibility == Visibility.HIDDEN) {
            return !element.isDisplayed();
        } else {
            return true;
        }
    }
    
    private List<WebElement> filterByVisibility(List<WebElement> elements, Visibility visibility) {
        return elements.stream()
                .filter(e -> filterByVisibility(e, visibility))
                .collect(Collectors.toList());
    }

    private String getErrorMessage(Range expectedSize, Visibility visibility,
                                   List<WebElement> elements) {
        long all = filterByVisibility(elements, Visibility.ALL).size();
        long displayed = filterByVisibility(elements, Visibility.DISPLAYED).size();
        long hidden = filterByVisibility(elements, Visibility.HIDDEN).size();
        return String.format(TIMEOUT_MESSAGE_PATTERN,
                expectedSize,
                visibility == Visibility.ALL ? "" : visibility.toString().toLowerCase(),
                all,
                displayed,
                hidden);
    }
    
}
