package com.naveen.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class SimpleSeleniumApp {

    private static final Path RESUME_DIR = Path.of("src", "main", "resources");

    public static void main(String[] args) throws InterruptedException {
        boolean headless = args.length > 0 && "--headless".equalsIgnoreCase(args[0]);

        String email = requiredEnv("NAUKRI_EMAIL");
        String password = requiredEnv("NAUKRI_PASSWORD");
        String resumeFileName = requiredEnv("RESUME_FILE_NAME");
        Path resumePath = RESUME_DIR.resolve(resumeFileName).toAbsolutePath();
        if (!Files.exists(resumePath)) {
            throw new IllegalArgumentException("Resume file not found at: " + resumePath);
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,1000");
        options.addArguments("--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get("https://www.naukri.com/nlogin/login");

            WebElement emailInput = waitForFirstVisible(wait,
                    By.id("usernameField"));
            System.out.println("Logging in with email: " + email);
            emailInput.clear();
            emailInput.sendKeys(email);

            WebElement passwordInput = waitForFirstVisible(wait,
                    By.id("passwordField"));
            System.out.println("Logging in with password: " + "*".repeat(password.length()));
            passwordInput.clear();
            passwordInput.sendKeys(password);

            WebElement loginButton = waitForFirstVisible(wait,
                    By.cssSelector("button[type='submit']"));
            System.out.println("Clicking login button");
            loginButton.click();
            Thread.sleep(4000);

            WebElement myProfile = waitForFirstVisible(wait,
                    By.cssSelector("a[href*='/mnjuser/profile']"),
                    By.cssSelector("a[title*='Profile']"));
            myProfile.click();
            Thread.sleep(2000);

            WebElement resumeFileInput = waitForFirstVisible(wait,
                    By.id("attachCV"),
                    By.cssSelector("input[type='file']"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", resumeFileInput);
            Thread.sleep(1000);

            resumeFileInput.sendKeys(resumePath.toString());
            Thread.sleep(5000);

            System.out.println("Resume upload completed!");
        } catch (Exception ex) {
            System.err.println("Naukri run failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw ex;
        } finally {
            driver.quit();
        }
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static WebElement waitForFirstVisible(WebDriverWait wait, By... selectors) {
        TimeoutException lastException = null;
        for (By selector : selectors) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            } catch (TimeoutException ex) {
                lastException = ex;
            }
        }
        throw new TimeoutException("Could not find element for selectors", lastException);
    }
}

