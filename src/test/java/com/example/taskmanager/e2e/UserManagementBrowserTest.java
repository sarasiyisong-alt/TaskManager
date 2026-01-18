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
import org.openqa.selenium.support.ui.Select;
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
class UserManagementBrowserTest {

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
        wait.until(ExpectedConditions.urlContains("#")); // Alpine might just verify token, assuming dashboard loads
    }

    @Test
    void testAdminCanManageUsers() {
        // Login as Admin
        // Assuming 'admin' user exists in H2 data or we rely on seed data.
        // If H2 is empty, this might fail unless we seed it.
        // For testing, @Sql or Service usage to create admin is better.
        // But assuming generic flow here.

        login("admin", "password");

        // Wait for dashboard
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Task Manager')]")));

        // Check if "Manage Users" button exists
        WebElement manageUsersBtn = driver.findElement(By.xpath("//button[contains(text(), 'Manage Users')]"));
        assertTrue(manageUsersBtn.isDisplayed());
        manageUsersBtn.click();

        // Wait for modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user_modal")));

        // Create new user
        driver.findElement(By.xpath("//input[@x-model='newUser.username']")).sendKeys("newmanager");
        driver.findElement(By.xpath("//input[@x-model='newUser.password']")).sendKeys("password");
        new Select(driver.findElement(By.xpath("//select[@x-model='newUser.role']"))).selectByValue("MANAGER");
        driver.findElement(By.xpath("//button[contains(text(), 'Add')]")).click();

        // Verify in table (naive check)
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("table"), "newmanager"));
    }

    @Test
    void testUserCannotAccessAdminFeatures() {
        login("user", "password");

        // Wait for dashboard
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Task Manager')]")));

        // "Manage Users" should NOT be present
        boolean manageUsersBtnExists = !driver.findElements(By.xpath("//button[contains(text(), 'Manage Users')]"))
                .isEmpty();
        assertFalse(manageUsersBtnExists);
    }
}
