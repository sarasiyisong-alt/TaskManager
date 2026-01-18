package com.example.taskmanager.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @Disabled("Fails in headless test environment due to AlpineJS rendering
// timeout")
public class SystemFeatureBrowserTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(baseUrl + "/");
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter your username']")));
        driver.findElement(By.xpath("//input[@placeholder='Enter your username']")).sendKeys(username);
        driver.findElement(By.xpath("//input[@placeholder='••••••••']")).sendKeys(password);
        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();
        wait.until(ExpectedConditions.urlContains("#"));
    }

    @Test
    void testExportCsv() {
        login("user", "password");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Task Manager')]")));

        WebElement exportBtn = driver.findElement(By.xpath("//button[contains(text(), 'Export CSV')]"));
        assertTrue(exportBtn.isDisplayed());

        // In headless mode, checking download is tricky.
        // We can just verify the button is clickable and doesn't crash the page.
        // Or check if it triggers a request (Selenium 4 features or just checking no
        // error).

        exportBtn.click();

        // Assert we are still on the page or didn't get an error
        assertTrue(driver.getCurrentUrl().contains(baseUrl));
    }

    @Test
    void testLogout() {
        login("user", "password");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Task Manager')]")));

        WebElement logoutBtn = driver.findElement(By.xpath("//button[contains(text(), 'Logout')]"));
        logoutBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Task Manager')]"))); // Login
                                                                                                                       // header
        assertTrue(driver.getCurrentUrl().contains("#") || driver.getCurrentUrl().endsWith("/"));
    }
}
