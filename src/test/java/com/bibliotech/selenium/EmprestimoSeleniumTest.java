package com.bibliotech.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class EmprestimoSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String testCaseId;
    private boolean testPassed = false;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        fazerLogin();
    }

    void fazerLogin() {
        driver.get("http://localhost:8080/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")))
            .sendKeys("admin@bibliotech.com");
        driver.findElement(By.id("senha")).sendKeys("admin123");
        driver.findElement(By.id("btn-login")).click();

        // Aguarda redirecionamento para dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
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
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("usuarioId")));

            // Seleciona primeiro usuário e primeiro livro habilitados
            driver.findElement(By.cssSelector("select[name='usuarioId'] option:not([disabled]):not(:empty)")).click();
            driver.findElement(By.cssSelector("select[name='livroId'] option:not([disabled]):not(:empty)")).click();
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Aguarda a mensagem de sucesso
            WebElement sucesso = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-success")));
            Assertions.assertTrue(sucesso.getText().toLowerCase().contains("realizado"), 
                "Mensagem de sucesso não encontrada: " + sucesso.getText());
            testPassed = true;
        } catch (AssertionError | TimeoutException e) {
            testPassed = false;
            takeScreenshot("falhas", testCaseId + "_erro");
            throw e;
        }
    }

    @Test
    @DisplayName("TS-010 - Exibe erro ao tentar emprestar livro indisponível")
    void deveExibirErroLivroIndisponivel() {
        testCaseId = "TS-010";
        try {
            driver.get("http://localhost:8080/emprestimos/novo");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("livroId")));

            // Seleciona usuário
            driver.findElement(By.cssSelector("select[name='usuarioId'] option:not([disabled])")).click();

            // Procura livro com "0 disponíveis"
            WebElement livroSelect = driver.findElement(By.name("livroId"));
            List<WebElement> options = livroSelect.findElements(By.tagName("option"));
            WebElement livroIndisponivel = null;

            for (WebElement opt : options) {
                String text = opt.getText();
                if (text.contains("0 disponíveis") && !opt.getAttribute("disabled").equals("true")) {
                    livroIndisponivel = opt;
                    break;
                }
            }

            if (livroIndisponivel == null) {
                Assertions.fail("Nenhum livro com 0 exemplares disponíveis encontrado. Certifique-se de que há livros indisponíveis no sistema.");
            }

            livroIndisponivel.click();
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Aguarda mensagem de erro
            WebElement erro = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-danger")));
            Assertions.assertTrue(erro.getText().toLowerCase().contains("indisponível"), 
                "Mensagem de erro inesperada: " + erro.getText());
            testPassed = true;
        } catch (AssertionError | TimeoutException e) {
            testPassed = false;
            takeScreenshot("falhas", testCaseId + "_erro");
            throw e;
        }
    }

    @Test
    @DisplayName("TS-011 - Registra devolução sem multa (no prazo)")
    void deveDevolverSemMulta() {
        testCaseId = "TS-011";
        try {
            driver.get("http://localhost:8080/emprestimos?filtro=ativos");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

            // Verifica se há pelo menos um empréstimo ativo com link "Devolver"
            List<WebElement> links = driver.findElements(By.linkText("Devolver"));
            if (links.isEmpty()) {
                Assertions.fail("Nenhum empréstimo ativo encontrado para devolução.");
            }

            links.get(0).click();

            // Aguarda mensagem de sucesso
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-success")));
            String texto = msg.getText().toLowerCase();
            Assertions.assertTrue(texto.contains("sem multa") || texto.contains("r$ 0,00"), 
                "Mensagem inesperada: " + msg.getText());
            testPassed = true;
        } catch (AssertionError | TimeoutException e) {
            testPassed = false;
            takeScreenshot("falhas", testCaseId + "_erro");
            throw e;
        }
    }

    @Test
    @DisplayName("BUG: TS-012 - Calcula multa como R$ 3,00/dia (deve ser R$ 2,00)")
    void deveCalcularMultaIncorreta() {
        testCaseId = "TS-012";
        try {
            driver.get("http://localhost:8080/emprestimos?filtro=ativos");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

            List<WebElement> links = driver.findElements(By.linkText("Devolver"));
            if (links.isEmpty()) {
                Assertions.fail("Nenhum empréstimo com atraso encontrado para teste de multa.");
            }

            links.get(0).click();

            // Aguarda mensagem de multa (deve ser .alert-info)
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-info")));
            String texto = msg.getText();
            // O sistema mostra R$ 6,00 → bug confirmado se esperado era R$ 4,00
            Assertions.assertTrue(texto.contains("R$ 4,00"), 
                "Esperava multa de R$ 4,00, mas recebeu: " + texto);
            testPassed = true;
        } catch (AssertionError | TimeoutException e) {
            testPassed = false;
            takeScreenshot("falhas", testCaseId + "_erro");
            throw e;
        }
    }

    @Test
    @DisplayName("BUG: TS-013 - Prazo de empréstimo = 7 dias (deve ser 14)")
    void deveMostrarPrazoDe7Dias() {
        testCaseId = "TS-013";
        try {
            driver.get("http://localhost:8080/emprestimos/novo");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Procura por qualquer <small> que contenha info de prazo
            List<WebElement> smalls = driver.findElements(By.tagName("small"));
            WebElement info = null;
            for (WebElement s : smalls) {
                if (s.getText().contains("dia")) {
                    info = s;
                    break;
                }
            }

            if (info == null) {
                Assertions.fail("Nenhum elemento <small> com informação de prazo encontrado.");
            }

            String texto = info.getText();
            Assertions.assertTrue(texto.contains("14 dias"), 
                "Esperava '14 dias', mas encontrou: " + texto);
            testPassed = true;
        } catch (AssertionError | TimeoutException e) {
            testPassed = false;
            takeScreenshot("falhas", testCaseId + "_erro");
            throw e;
        }
    }
}