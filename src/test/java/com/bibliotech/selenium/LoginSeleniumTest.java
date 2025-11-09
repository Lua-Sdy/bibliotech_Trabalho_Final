package com.bibliotech.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class LoginSeleniumTest {

    private WebDriver driver;
    private String testCaseId;
    private boolean testPassed = false; // começa como falha até provar o contrário

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        if (testCaseId != null) {
            String subfolder = testPassed ? "sucessos" : "falhas";
            String fileName = testCaseId + "_" + (testPassed ? "sucesso" : "falha");
            takeScreenshot(subfolder, fileName);
        }
        if (driver != null) {
            driver.quit();
        }
    }

    private void takeScreenshot(String subfolder, String fileName) {
        try {
            Path dir = Paths.get("evidencias", "screenshots", subfolder).toAbsolutePath();
            dir.toFile().mkdirs(); // Cria pastas se não existirem

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destino = dir.resolve(fileName + ".png");

            Files.copy(screenshot.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📸 Screenshot salvo: " + destino.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================== TESTES ===================

    @Test
    @DisplayName("BUG: TS-001 - Login com admin@bibliotech.com / admin123 falha")
    void deveFalharNoLoginDoAdmin() {
        testCaseId = "TS-001";
        try {
            driver.get("http://localhost:8080/login");
            driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com");
            driver.findElement(By.id("senha")).sendKeys("admin123");
            driver.findElement(By.id("btn-login")).click();

            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains("/dashboard"), "Esperava redirecionamento para /dashboard");
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e; // Falha = BUG CONFIRMADO
        }
    }

    @Test
    @DisplayName("TS-002 - Exibe erro ao tentar login com senha incorreta")
    void deveExibirErroComSenhaIncorreta() {
        testCaseId = "TS-002";
        try {
            driver.get("http://localhost:8080/login");
            driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com");
            driver.findElement(By.id("senha")).sendKeys("senhaerrada");
            driver.findElement(By.id("btn-login")).click();

            WebElement erro = driver.findElement(By.className("alert-danger"));
            Assertions.assertTrue(erro.isDisplayed(), "Mensagem de erro deve aparecer");
            Assertions.assertTrue(erro.getText().contains("inválidos"), "Mensagem deve conter 'inválidos'");
            testPassed = false;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }
}