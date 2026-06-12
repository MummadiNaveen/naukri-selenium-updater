package com.naveen.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class SimpleSeleniumApp {

    private static final Path RESUME_DIR = Path.of("src", "main", "resources");
    private static final Path DEBUG_DIR = Path.of("target", "debug");

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
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");
        options.addArguments("--accept-lang=en-US,en;q=0.9");
        options.addArguments("--lang=en-US");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        if (headless) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        // Hide webdriver fingerprint to avoid bot detection
        ((JavascriptExecutor) driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
            "window.chrome = {runtime: {}};" +
            "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});" +
            "Object.defineProperty(navigator, 'languages', {get: () => ['en-US']});"
        );

        try {
            driver.get("https://www.naukri.com/nlogin/login");
            sleep(4000);
            System.out.println("Login page title: " + driver.getTitle());

            failIfAccessDenied(driver, "login-page");

            WebElement emailInput = findFirst(driver, 3, 1500,
                    By.id("usernameField"),
                    By.cssSelector("input[placeholder*='Email ID']"),
                    By.cssSelector("input[type='text']"));
            WebElement passwordInput = findFirst(driver, 3, 1500,
                    By.id("passwordField"),
                    By.cssSelector("input[type='password']"));
            emailInput.clear();
            emailInput.sendKeys(email);
            passwordInput.clear();
            passwordInput.sendKeys(password);

            clickFirst(driver, 3, 1500,
                    By.cssSelector("button.blue-btn"),
                    By.cssSelector("button[type='submit']"),
                    By.xpath("//button[contains(normalize-space(), 'Login') or contains(normalize-space(), 'login')]")
            );
            sleep(7000);

            System.out.println("Post-login title: " + driver.getTitle());
            System.out.println("Post-login URL: " + driver.getCurrentUrl());

            failIfAccessDenied(driver, "post-login");
            failIfOtpChallenge(driver);

            try {
                clickFirst(driver, 2, 1500,
                        By.cssSelector("a[href*='/mnjuser/profile']"),
                        By.cssSelector("a[title*='Profile']"));
            } catch (Exception e) {
                System.out.println("Profile link not found, navigating directly to profile URL");
                driver.get("https://www.naukri.com/mnjuser/profile");
            }
            sleep(4000);

            failIfAccessDenied(driver, "profile-page");

            WebElement resumeFileInput = findFirst(driver, 3, 1500,
                    By.id("attachCV"),
                    By.cssSelector("input[type='file']"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", resumeFileInput);
            sleep(1000);

            resumeFileInput.sendKeys(resumePath.toString());
            sleep(5000);

            List<WebElement> updateButtons = driver.findElements(By.cssSelector("input[value='Update resume']"));
            if (!updateButtons.isEmpty()) {
                updateButtons.get(0).click();
                sleep(3000);
            }

            System.out.println("Resume upload completed!");
        } catch (Exception ex) {
            System.err.println("Naukri run failed: " + ex.getMessage());
            dumpDebugArtifacts(driver);
            throw ex;
        } finally {
            driver.quit();
        }
    }

    private static void failIfAccessDenied(WebDriver driver, String stage) {
        String title = safeLower(driver.getTitle());
        String url = safeLower(driver.getCurrentUrl());
        String source = safeLower(driver.getPageSource());
        if (title.contains("access denied") || source.contains("access denied") || url.contains("accessdenied")) {
            throw new IllegalStateException("Access denied detected at stage: " + stage);
        }
    }

    private static void failIfOtpChallenge(WebDriver driver) {
        String source = safeLower(driver.getPageSource());
        String title = safeLower(driver.getTitle());
        if (source.contains("otp") || source.contains("one time password") || title.contains("otp")) {
            throw new IllegalStateException("OTP challenge detected. This run environment is not trusted by Naukri.");
        }
    }

    private static WebElement findFirst(WebDriver driver, int retries, long delayMs, By... selectors) throws InterruptedException {
        RuntimeException last = null;
        for (int attempt = 0; attempt < retries; attempt++) {
            for (By selector : selectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            }
            sleep(delayMs);
        }
        if (last != null) {
            throw last;
        }
        throw new IllegalStateException("Could not find required element");
    }

    private static void clickFirst(WebDriver driver, int retries, long delayMs, By... selectors) throws InterruptedException {
        for (int attempt = 0; attempt < retries; attempt++) {
            for (By selector : selectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    elements.get(0).click();
                    return;
                }
            }
            sleep(delayMs);
        }
        throw new IllegalStateException("Could not click required element");
    }

    private static void dumpDebugArtifacts(WebDriver driver) {
        try {
            Files.createDirectories(DEBUG_DIR);
            String stamp = String.valueOf(System.currentTimeMillis());
            Path htmlPath = DEBUG_DIR.resolve("page-" + stamp + ".html");
            Files.writeString(htmlPath, driver.getPageSource());

            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Files.write(DEBUG_DIR.resolve("screen-" + stamp + ".png"), screenshot);
            }
            System.err.println("Saved debug artifacts under: " + DEBUG_DIR.toAbsolutePath());
        } catch (IOException ioEx) {
            System.err.println("Failed to save debug artifacts: " + ioEx.getMessage());
        }
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + key);
        }
        return value;
    }
}
