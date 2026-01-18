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

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @Disabled("Fails in headless test environment due to AlpineJS rendering
// timeout")
public class TaskManagementBrowserTest {

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
        options.addArguments("--headless=new"); // Use new headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

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

        // Wait for login form
        WebElement usernameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter your username']")));
        WebElement passwordInput = driver.findElement(By.xpath("//input[@placeholder='••••••••']"));
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]"));

        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        submitButton.click();

        // Wait for dashboard or error
        // If success, we expect logout button or "Task Manager" header in dashboard
        // wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),
        // 'Logout')]")));
    }

    @Test
    void testCreateAndListTask() {
        login("user", "password");

        // Wait for dashboard and New Task button
        wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath("//button[contains(text(), '+ New Task')]")));
        driver.findElement(By.xpath("//button[contains(text(), '+ New Task')]")).click();

        // Wait for modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("task_modal")));

        // Fill form
        String taskTitle = "Selenium Task " + System.currentTimeMillis();
        driver.findElement(By.xpath("//input[@x-model='newTask.title']")).sendKeys(taskTitle);
        driver.findElement(By.xpath("//textarea[@x-model='newTask.description']"))
                .sendKeys("Description for test task");
        driver.findElement(By.xpath("//input[@x-model='newTask.priority']")).clear();
        driver.findElement(By.xpath("//input[@x-model='newTask.priority']")).sendKeys("1");

        // Submit
        driver.findElement(By.xpath("//button[contains(text(), 'Create Task')]")).click();

        // Verify in list
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h3"), taskTitle));

        // Verify status badge
        WebElement statusBadge = driver.findElement(By.xpath("//span[contains(text(), 'PENDING')]"));
        assertTrue(statusBadge.isDisplayed());
    }

    @Test
    void testCalendarViewSwitch() {
        login("user", "password");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Calendar')]")));
        driver.findElement(By.xpath("//button[contains(text(), 'Calendar')]")).click();

        // Verify Calendar Grid is visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Sun')]")));
        assertTrue(driver.findElement(By.xpath("//div[contains(text(), 'Mon')]")).isDisplayed());
    }
}
