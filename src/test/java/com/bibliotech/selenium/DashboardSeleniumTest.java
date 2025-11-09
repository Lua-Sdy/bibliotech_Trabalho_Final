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

public class DashboardSeleniumTest {

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

        // Aguarda redirecionamento para o dashboard
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
    @DisplayName("TS-014 - Exibe dashboard com estatísticas corretas")
    void deveExibirDashboardCorreto() {
        testCaseId = "TS-014";
        try {
            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains("/dashboard"), "Não redirecionou para o dashboard");

            WebElement cardLivros = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("total-livros")));
            WebElement cardUsuarios = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usuarios-ativos")));

            int totalLivros = Integer.parseInt(cardLivros.getText().trim());
            int usuariosAtivos = Integer.parseInt(cardUsuarios.getText().trim());

            Assertions.assertTrue(totalLivros > 0, "Total de livros deve ser > 0");
            Assertions.assertTrue(usuariosAtivos > 0, "Usuários ativos deve ser > 0");

            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }

    @Test
    @DisplayName("TS-015 - Impede exclusão de livro com empréstimos ativos")
    void deveImpedirExclusaoLivroComEmprestimos() {
        testCaseId = "TS-015";
        try {
            driver.get("http://localhost:8080/livros");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

            // Procura linha com "Clean Code"
            List<WebElement> linhas = driver.findElements(By.cssSelector("table tbody tr"));
            WebElement linhaCleanCode = null;
            for (WebElement linha : linhas) {
                if (linha.getText().contains("Clean Code")) {
                    linhaCleanCode = linha;
                    break;
                }
            }

            Assertions.assertNotNull(linhaCleanCode, "Livro 'Clean Code' não encontrado na lista");

            // Agora procura o link de exclusão: <a class="btn btn-sm btn-danger">
            WebElement botaoExcluir = wait.until(ExpectedConditions.elementToBeClickable(
                linhaCleanCode.findElement(By.cssSelector("a.btn-danger"))
            ));
            botaoExcluir.click();

            // Aceita o alerta de confirmação
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
            } catch (TimeoutException ignored) {
                // Nenhum alerta apareceu – ok
            }

            // Verifica se aparece mensagem de erro OU se o livro ainda está listado
            boolean mensagemErroPresente = false;
            boolean livroAindaExiste = false;

            try {
                WebElement mensagemErro = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-danger")));
                mensagemErroPresente = mensagemErro.getText().contains("ativos");
            } catch (TimeoutException ignored) {
                // Sem mensagem de erro
            }

            // Atualiza a página para verificar se o livro ainda está lá
            driver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            livroAindaExiste = driver.getPageSource().contains("Clean Code");

            Assertions.assertTrue(
                mensagemErroPresente || livroAindaExiste,
                "Esperava impedir exclusão de livro com empréstimo ativo"
            );

            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }
}