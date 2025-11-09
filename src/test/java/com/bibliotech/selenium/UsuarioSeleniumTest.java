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

public class UsuarioSeleniumTest {

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
        fazerLogin();
    }

    void fazerLogin() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com");
        driver.findElement(By.id("senha")).sendKeys("admin123");
        driver.findElement(By.id("btn-login")).click();
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
            dir.toFile().mkdirs(); // Cria todas as pastas necessárias

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
    @DisplayName("TS-006 - Cadastra novo usuário com sucesso")
    void deveCadastrarNovoUsuario() {
        testCaseId = "TS-006";
        try {
            driver.get("http://localhost:8080/usuarios/novo");
            driver.findElement(By.id("nome")).sendKeys("Ana Souza");
            driver.findElement(By.id("email")).sendKeys("ana@email.com");
            driver.findElement(By.id("cpf")).sendKeys("444.444.444-44");
            driver.findElement(By.id("senha")).sendKeys("senha123");
            driver.findElement(By.id("tipo")).sendKeys("ALUNO");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement sucesso = driver.findElement(By.className("alert-success"));
            Assertions.assertTrue(sucesso.getText().contains("sucesso"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("TS-007 - Exibe erro ao tentar cadastrar CPF inválido")
    void deveExibirErroCPFInvalido() {
        testCaseId = "TS-007";
        try {
            driver.get("http://localhost:8080/usuarios/novo");
            driver.findElement(By.id("nome")).sendKeys("Carlos");
            driver.findElement(By.id("email")).sendKeys("carlos2@email.com");
            driver.findElement(By.id("cpf")).sendKeys("123"); // inválido
            driver.findElement(By.id("senha")).sendKeys("senha123");
            driver.findElement(By.id("tipo")).sendKeys("ALUNO");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement erro = driver.findElement(By.className("alert-danger"));
            Assertions.assertTrue(erro.getText().contains("CPF"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("TS-008 - Exibe erro ao tentar cadastrar email já existente")
    void deveExibirErroEmailDuplicado() {
        testCaseId = "TS-008";
        try {
            driver.get("http://localhost:8080/usuarios/novo");
            driver.findElement(By.id("nome")).sendKeys("Usuário Duplicado");
            driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com"); // já existe
            driver.findElement(By.id("cpf")).sendKeys("555.555.555-55");
            driver.findElement(By.id("senha")).sendKeys("senha123");
            driver.findElement(By.id("tipo")).sendKeys("ALUNO");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement erro = driver.findElement(By.className("alert-danger"));
            Assertions.assertTrue(erro.getText().contains("Email já cadastrado"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }
}