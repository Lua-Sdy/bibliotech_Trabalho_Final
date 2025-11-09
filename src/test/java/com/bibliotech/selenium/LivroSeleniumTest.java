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

public class LivroSeleniumTest {

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
    @DisplayName("TS-003 - Cadastra novo livro com sucesso")
    void deveCadastrarNovoLivro() {
        testCaseId = "TS-003";
        try {
            driver.get("http://localhost:8080/livros/novo");
            driver.findElement(By.id("titulo")).sendKeys("Domain-Driven Design");
            driver.findElement(By.id("autor")).sendKeys("Eric Evans");
            driver.findElement(By.id("isbn")).sendKeys("978-0321125217");
            driver.findElement(By.id("editora")).sendKeys("Addison-Wesley");
            driver.findElement(By.id("ano")).sendKeys("2003");
            driver.findElement(By.id("quantidadeExemplares")).sendKeys("2");
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
    @DisplayName("BUG: TS-004 - Permite ISBN duplicado (deve falhar)")
    void deveExibirErroISBNDuplicado() {
        testCaseId = "TS-004";
        try {
            // Cadastra primeiro livro
            driver.get("http://localhost:8080/livros/novo");
            driver.findElement(By.id("titulo")).sendKeys("Livro A");
            driver.findElement(By.id("autor")).sendKeys("Autor A");
            driver.findElement(By.id("isbn")).sendKeys("978-0000000000");
            driver.findElement(By.id("ano")).sendKeys("2020");
            driver.findElement(By.id("quantidadeExemplares")).sendKeys("1");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Tenta cadastrar com mesmo ISBN
            driver.get("http://localhost:8080/livros/novo");
            driver.findElement(By.id("titulo")).sendKeys("Livro B");
            driver.findElement(By.id("autor")).sendKeys("Autor B");
            driver.findElement(By.id("isbn")).sendKeys("978-0000000000"); // REPETIDO!
            driver.findElement(By.id("ano")).sendKeys("2021");
            driver.findElement(By.id("quantidadeExemplares")).sendKeys("1");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Espera mensagem de erro
            WebElement erro = driver.findElement(By.className("alert-danger"));
            Assertions.assertTrue(erro.getText().contains("ISBN já cadastrado"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e; // Falha = BUG CONFIRMADO
        }
    }

    @Test
    @DisplayName("TS-005 - Busca livro por título (case-insensitive)")
    void deveBuscarLivroPorTitulo() {
        testCaseId = "TS-005";
        try {
            driver.get("http://localhost:8080/livros");
            driver.findElement(By.id("busca")).sendKeys("clean code");
            driver.findElement(By.id("btn-buscar")).click();

            WebElement resultado = driver.findElement(By.tagName("table"));
            Assertions.assertTrue(resultado.getText().contains("Clean Code"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }
}