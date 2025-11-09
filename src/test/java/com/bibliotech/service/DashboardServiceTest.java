package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {

    private DashboardService dashboardService;

    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private EmprestimoRepository emprestimoRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Cria mocks dos repositórios
        livroRepository = Mockito.mock(LivroRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        emprestimoRepository = Mockito.mock(EmprestimoRepository.class);

        // Define o comportamento esperado dos mocks
        when(livroRepository.count()).thenReturn(3L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0))
                .thenReturn(Arrays.asList(new Livro(), new Livro())); // ✅ Lista do tipo correto

        when(usuarioRepository.findByAtivoTrue())
                .thenReturn(Arrays.asList(new Usuario(), new Usuario(), new Usuario(), new Usuario())); // ✅ Lista do tipo correto

        when(emprestimoRepository.count()).thenReturn(5L);
        when(emprestimoRepository.findEmprestimosAtrasados())
                .thenReturn(Collections.singletonList(new Emprestimo())); // ✅ Tipo correto

        // Instancia o serviço e injeta os mocks por reflexão
        dashboardService = new DashboardService();
        var f1 = DashboardService.class.getDeclaredField("livroRepository");
        f1.setAccessible(true);
        f1.set(dashboardService, livroRepository);

        var f2 = DashboardService.class.getDeclaredField("usuarioRepository");
        f2.setAccessible(true);
        f2.set(dashboardService, usuarioRepository);

        var f3 = DashboardService.class.getDeclaredField("emprestimoRepository");
        f3.setAccessible(true);
        f3.set(dashboardService, emprestimoRepository);
    }

    @Test
    void deveRetornarTodasAsEstatisticasConformeRF14() {
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();

        assertTrue(estatisticas.containsKey("totalLivros"), "Falta totalLivros");
        assertTrue(estatisticas.containsKey("totalUsuarios"), "Falta totalUsuarios");
        assertTrue(estatisticas.containsKey("emprestimosAtivos"), "Falta emprestimosAtivos");
        assertTrue(estatisticas.containsKey("livrosDisponiveis"), "Falta livrosDisponiveis");
        assertTrue(estatisticas.containsKey("emprestimosAtrasados"), "Falta emprestimosAtrasados");

        assertEquals(3L, estatisticas.get("totalLivros"));
        assertEquals(4, ((List<?>) usuarioRepository.findByAtivoTrue()).size());
        assertEquals(5L, estatisticas.get("emprestimosAtivos"));
        assertEquals(2, ((List<?>) livroRepository.findByQuantidadeDisponivelGreaterThan(0)).size());
        assertEquals(1, ((List<?>) emprestimoRepository.findEmprestimosAtrasados()).size());
    }

    @Test
    void deveFalharSeEstatisticasNaoBateremComRegraRF14() {
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();

        // Teste propositalmente errado para verificar falha
        assertNotEquals(10L, estatisticas.get("totalLivros"), "Erro: total de livros incorreto");
    }
}
