package com.naveen.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;

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
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        if (headless) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        // Hide webdriver fingerprint to avoid bot detection
        ((JavascriptExecutor) driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        try {
            driver.get("https://www.naukri.com/nlogin/login");
            Thread.sleep(4000);
            System.out.println("Page title: " + driver.getTitle());

            driver.findElement(By.id("usernameField")).sendKeys(email);
            driver.findElement(By.id("passwordField")).sendKeys(password);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(7000);

            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());

            try {
                driver.findElement(By.cssSelector("a[href*='/mnjuser/profile']")).click();
            } catch (Exception e) {
                System.out.println("Profile link not found, navigating directly to profile URL");
                driver.get("https://www.naukri.com/mnjuser/profile");
            }
            Thread.sleep(4000);
            System.out.println("Page title: " + driver.getTitle());
            WebElement resumeFileInput = driver.findElement(By.id("attachCV"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", resumeFileInput);
            Thread.sleep(1000);

            resumeFileInput.sendKeys(resumePath.toString());
            Thread.sleep(5000);

            System.out.println("Resume upload completed!");
        } catch (Exception ex) {
            System.err.println("Naukri run failed: " + ex.getMessage());
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
}
