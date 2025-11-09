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

public class EmprestimoSeleniumTest {

    private WebDriver driver;
    private String testCaseId;
    private boolean testPassed = false;

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
            dir.toFile().mkdirs();

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
    @DisplayName("TS-009 - Realiza empréstimo com sucesso")
    void deveRealizarEmprestimo() {
        testCaseId = "TS-009";
        try {
            driver.get("http://localhost:8080/emprestimos/novo");
            // Seleciona primeiro usuário e primeiro livro
            driver.findElement(By.cssSelector("select[name='usuarioId'] option:not(:disabled)")).click();
            driver.findElement(By.cssSelector("select[name='livroId'] option:not(:disabled)")).click();
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement sucesso = driver.findElement(By.className("alert-success"));
            Assertions.assertTrue(sucesso.getText().contains("realizado"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("TS-010 - Exibe erro ao tentar emprestar livro indisponível")
    void deveExibirErroLivroIndisponivel() {
        testCaseId = "TS-010";
        try {
            // Primeiro, esgotamos todos os exemplares de um livro (ex: Clean Code)
            // Neste cenário, simulamos que o livro com menos exemplares já está esgotado
            driver.get("http://localhost:8080/emprestimos/novo");

            // Seleciona um usuário
            driver.findElement(By.cssSelector("select[name='usuarioId'] option:not(:disabled)")).click();

            // Verifica se há pelo menos um livro com 0 disponível
            boolean livroIndisponivelEncontrado = false;
            WebElement livroSelect = driver.findElement(By.name("livroId"));
            for (WebElement option : livroSelect.findElements(By.tagName("option"))) {
                if (option.getText().contains("0 disponíveis")) {
                    option.click();
                    livroIndisponivelEncontrado = true;
                    break;
                }
            }

            if (!livroIndisponivelEncontrado) {
                // Se não encontrar, falha com explicação clara
                Assertions.fail("Nenhum livro com 0 exemplares disponíveis encontrado para teste.");
            }

            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Verifica mensagem de erro
            WebElement erro = driver.findElement(By.className("alert-danger"));
            Assertions.assertTrue(erro.getText().contains("indisponível"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("TS-011 - Registra devolução sem multa (no prazo)")
    void deveDevolverSemMulta() {
        testCaseId = "TS-011";
        try {
            driver.get("http://localhost:8080/emprestimos?filtro=ativos");
            driver.findElement(By.linkText("Devolver")).click();

            WebElement msg = driver.findElement(By.className("alert-success"));
            String texto = msg.getText();
            Assertions.assertTrue(texto.contains("sem multa") || texto.contains("R$ 0,00"));
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("BUG: TS-012 - Calcula multa como R$ 3,00/dia (deve ser R$ 2,00)")
    void deveCalcularMultaIncorreta() {
        testCaseId = "TS-012";
        try {
            // Simula devolução com atraso (sistema deve cobrar R$ 6,00 em vez de R$ 4,00)
            driver.get("http://localhost:8080/emprestimos?filtro=ativos");
            driver.findElement(By.linkText("Devolver")).click();

            WebElement msg = driver.findElement(By.className("alert-info"));
            String texto = msg.getText();

            // O BUG está aqui: o sistema mostra R$ 6,00 para 2 dias → teste falha
            Assertions.assertTrue(texto.contains("R$ 4,00"), "Esperava multa de R$ 4,00 para 2 dias de atraso");
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e; // Falha = BUG CONFIRMADO
        }
    }

    @Test
    @DisplayName("BUG: TS-013 - Prazo de empréstimo = 7 dias (deve ser 14)")
    void deveMostrarPrazoDe7Dias() {
        testCaseId = "TS-013";
        try {
            driver.get("http://localhost:8080/emprestimos/novo");
            WebElement info = driver.findElement(By.tagName("small"));
            String texto = info.getText();
            // O BUG está aqui: sistema mostra "7 dias" → teste falha
            Assertions.assertTrue(texto.contains("14 dias"), "Esperava prazo de 14 dias");
            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e; // Falha = BUG CONFIRMADO
        }
    }
}