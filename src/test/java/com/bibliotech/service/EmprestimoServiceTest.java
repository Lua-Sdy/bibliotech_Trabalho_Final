package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.model.Usuario.TipoUsuario;
import com.bibliotech.repository.EmprestimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmprestimoServiceTest {

    private EmprestimoService emprestimoService;
    private EmprestimoRepository emprestimoRepository;
    private LivroService livroService;

    private Usuario usuario;
    private Livro livro;

    @BeforeEach
    void setup() {
        emprestimoRepository = mock(EmprestimoRepository.class);
        livroService = mock(LivroService.class);

        emprestimoService = new EmprestimoService();
        emprestimoService.setEmprestimoRepository(emprestimoRepository);
        emprestimoService.setLivroService(livroService);

        usuario = new Usuario();
        usuario.setNome("Maria");
        usuario.setAtivo(true);
        usuario.setTipo(TipoUsuario.ALUNO); // CORRETO

        livro = new Livro();
        livro.setTitulo("Java Avançado");
        livro.setQuantidadeDisponivel(3);
    }

    

    @Test
    void testRealizarEmprestimoComSucesso() {
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = emprestimoService.realizarEmprestimo(usuario, livro);

        assertEquals(LocalDate.now(), emprestimo.getDataEmprestimo());
        assertEquals(LocalDate.now().plusDays(14), emprestimo.getDataDevolucaoPrevista());
        verify(livroService, times(1)).decrementarDisponibilidade(livro);
    }

    @Test
    void testCalcularDataDevolucaoCorreta() {
        LocalDate dataEmprestimo = LocalDate.of(2025, 11, 2);
        LocalDate dataDevolucao = emprestimoService.calcularDataDevolucao(dataEmprestimo);
        assertEquals(LocalDate.of(2025, 11, 16), dataDevolucao);
    }

    @Test
    void testCalcularMultaSemAtraso() {
        Emprestimo emprestimo = new Emprestimo(usuario, livro, LocalDate.now(), LocalDate.now().plusDays(14));
        emprestimo.setDataDevolucaoReal(LocalDate.now().plusDays(14));

        double multa = emprestimoService.calcularMulta(emprestimo);
        assertEquals(0.0, multa);
    }

    @Test
    void testCalcularMultaComAtraso() {
        Emprestimo emprestimo = new Emprestimo(usuario, livro, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);
        assertEquals(12.0, multa); // 6 dias x 2
    }

    @Test
    void testRegistrarDevolucaoComSucesso() {
        Emprestimo emprestimo = new Emprestimo(usuario, livro, LocalDate.now().minusDays(15), LocalDate.now().minusDays(1));
        emprestimo.setId(1L);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo devolvido = emprestimoService.registrarDevolucao(1L);

        assertNotNull(devolvido.getDataDevolucaoReal());
        assertFalse(devolvido.getAtivo());
        verify(livroService, times(1)).incrementarDisponibilidade(livro);
    }

    @Test
    void testRegistrarDevolucaoDuplicadaLancaExcecao() {
        Emprestimo emprestimo = new Emprestimo(usuario, livro, LocalDate.now().minusDays(15), LocalDate.now().minusDays(1));
        emprestimo.setDataDevolucaoReal(LocalDate.now());
        emprestimo.setId(1L);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emprestimoService.registrarDevolucao(1L));

        assertEquals("Empréstimo já foi devolvido", exception.getMessage());
    }

    @Test
    void testRegistrarDevolucaoComMulta() {
        Emprestimo emprestimo = new Emprestimo(usuario, livro, LocalDate.now().minusDays(16), LocalDate.now().minusDays(2));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emprestimoRepository.findById(anyLong())).thenReturn(Optional.of(emprestimo));

        Emprestimo devolvido = emprestimoService.registrarDevolucao(1L);

        assertEquals(2 * 2.0, devolvido.getMulta()); // 2 dias de atraso x R$2
    }
    
    @Test
    void testRealizarEmprestimoQuandoIndisponivelLancaExcecao() {
        livro.setQuantidadeDisponivel(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            emprestimoService.realizarEmprestimo(usuario, livro)
        );

        assertEquals("Livro indisponível para empréstimo", exception.getMessage());
    }
}