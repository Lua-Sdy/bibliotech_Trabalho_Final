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

public class DashboardSeleniumTest {

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
    @DisplayName("TS-014 - Exibe dashboard com estatísticas corretas")
    void deveExibirDashboardCorreto() {
        testCaseId = "TS-014";
        try {
            // Garante que estamos no dashboard
            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains("/dashboard"), "Não redirecionou para o dashboard");

            // Busca cards de estatísticas
            WebElement cardLivros = driver.findElement(By.id("total-livros"));
            WebElement cardUsuarios = driver.findElement(By.id("usuarios-ativos"));

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
            // Acessa a lista de livros
            driver.get("http://localhost:8080/livros");

            // Encontra o livro "Clean Code" (assumindo que está emprestado)
            // Procura pelo título na tabela
            WebElement tabela = driver.findElement(By.tagName("table"));
            WebElement linhaCleanCode = null;

            for (WebElement linha : tabela.findElements(By.tagName("tr"))) {
                if (linha.getText().contains("Clean Code")) {
                    linhaCleanCode = linha;
                    break;
                }
            }

            Assertions.assertNotNull(linhaCleanCode, "Livro 'Clean Code' não encontrado na lista");

            // Clica no botão de exclusão (assumindo que tem um botão com classe 'btn-excluir')
            WebElement botaoExcluir = linhaCleanCode.findElement(By.cssSelector("button.btn-danger"));
            botaoExcluir.click();

            // Aceita o alert de confirmação (se houver)
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException e) {
                // Sem alerta, ok
            }

            // Verifica se aparece mensagem de erro
            WebElement mensagemErro = null;
            try {
                mensagemErro = driver.findElement(By.className("alert-danger"));
            } catch (NoSuchElementException ignored) {
                // Se não encontrar, pode estar na página de erro ou redirecionado
            }

            // Também verifica se o livro ainda está na lista (não foi excluído)
            driver.navigate().refresh();
            boolean livroAindaExiste = driver.getPageSource().contains("Clean Code");

            Assertions.assertTrue(
                (mensagemErro != null && mensagemErro.getText().contains("ativos")) ||
                livroAindaExiste,
                "Esperava impedir exclusão de livro com empréstimo ativo"
            );

            testPassed = true;
        } catch (AssertionError e) {
            testPassed = false;
            throw e;
        }
    }
}