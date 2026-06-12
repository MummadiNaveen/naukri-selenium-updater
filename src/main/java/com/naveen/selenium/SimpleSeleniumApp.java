package com.naveen.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;

import java.nio.file.Path;
import java.time.Duration;

public class SimpleSeleniumApp {

    public static void main(String[] args) throws InterruptedException {
        boolean headless = args.length > 0 && "--headless".equalsIgnoreCase(args[0]);

        String email = "naveenjr15@gmail.com";
        String password = "Nav@15M7E";
        String resumeFile = "src/main/resources/Gowtham_sv_jr.pdf";

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,1000");
        options.addArguments("--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

        try {
            driver.get("https://www.naukri.com/nlogin/login");

            WebElement emailInput = driver.findElement(By.cssSelector("input[placeholder*='Email ID']"));
            emailInput.clear();
            emailInput.sendKeys(email);

            WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
            passwordInput.clear();
            passwordInput.sendKeys(password);

            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();
            Thread.sleep(4000);

            WebElement myProfile = driver.findElement(By.cssSelector("a[href*=\"/mnjuser/profile\"]"));
            myProfile.click();
            Thread.sleep(2000);

            WebElement resumeFileInput = driver.findElement(By.id("attachCV"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", resumeFileInput);
            Thread.sleep(1000);

            Path resumePath = Path.of(resumeFile);
            resumeFileInput.sendKeys(resumePath.toAbsolutePath().toString());
            Thread.sleep(5000);

            System.out.println("Resume upload completed!");
        } finally {
            driver.quit();
        }
    }
}

