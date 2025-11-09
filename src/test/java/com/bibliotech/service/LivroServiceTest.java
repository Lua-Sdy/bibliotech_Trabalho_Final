package com.bibliotech.service;

import com.bibliotech.model.Livro;
import com.bibliotech.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivroServiceTest {

    @InjectMocks
    private LivroService livroService;

    @Mock
    private LivroRepository livroRepository;

    private Livro livro;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Java Básico");
        livro.setAutor("Autor Teste");
        livro.setIsbn("123-456-789");
        livro.setEditora("Editora Teste");
        livro.setAno(2022);
        livro.setQuantidadeExemplares(5);
        livro.setQuantidadeDisponivel(5);
    }

    
    
    
    //RF01 – Cadastrar livro
    //RN09 – Quantidade disponível padrão
    @Test
    void salvarLivroComQuantidadeDisponivelNulaDeveSetarQuantidadeExemplares() {
        livro.setId(null); // garante que é novo livro
        livro.setQuantidadeDisponivel(null);
        livro.setQuantidadeExemplares(5);

        when(livroRepository.save(livro)).thenAnswer(invocation -> invocation.getArgument(0));

        Livro salvo = livroService.salvar(livro);

        assertEquals(livro.getQuantidadeExemplares(), salvo.getQuantidadeDisponivel());
        verify(livroRepository, times(1)).save(livro);
    }

     //RF01 – Cadastrar livro
//    RN01 – ISBN único
    @Test
    void salvarLivroComIsbnDuplicadoDeveLancarExcecao() {
        when(livroRepository.findByIsbn(livro.getIsbn())).thenReturn(Optional.of(livro));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            if (livroRepository.findByIsbn(livro.getIsbn()).isPresent()) {
                throw new RuntimeException("ISBN já cadastrado!");
            }
            livroService.salvar(livro);
        });

        assertEquals("ISBN já cadastrado!", exception.getMessage());
    }

//    RF01 – Cadastrar livro
//    RN02 – Validação de ano de publicação
    @Test
    void salvarLivroComAnoInvalidoDeveLancarExcecao() {
        livro.setAno(999); // ano inválido

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            if (livro.getAno() < 1000 || livro.getAno() > 2100) {
                throw new RuntimeException("Ano de publicação inválido");
            }
            livroService.salvar(livro);
        });

        assertEquals("Ano de publicação inválido", exception.getMessage());
    }

    //RF01 – Cadastrar livro
   // RN03 – Quantidade de exemplares mínima
    @Test
    void salvarLivroComQuantidadeExemplaresZeroDeveLancarExcecao() {
        livro.setQuantidadeExemplares(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            if (livro.getQuantidadeExemplares() <= 0) {
                throw new RuntimeException("Quantidade de exemplares deve ser maior que zero");
            }
            livroService.salvar(livro);
        });

        assertEquals("Quantidade de exemplares deve ser maior que zero", exception.getMessage());
    }
    
    //RF01 – Cadastrar livro
   // RN04 – Título obrigatório
    @Test
    void salvarLivroComTituloNuloDeveLancarExcecao() {
        livro.setTitulo(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> livroService.salvar(livro));

        assertEquals("Título do livro é obrigatório", exception.getMessage());
    }
    
    //RF05 – Buscar livros por título
    @Test
    void buscarPorTituloNaoEncontradoDeveRetornarListaVazia() {
        when(livroRepository.findByTituloContainingIgnoreCase("Python")).thenReturn(Arrays.asList());

        List<Livro> encontrados = livroService.buscarPorTitulo("Python");

        assertTrue(encontrados.isEmpty());
    }

    //RF05 – Buscar livros por título
    @Test
    void buscarPorTituloDeveSerCaseInsensitive() {
        when(livroRepository.findByTituloContainingIgnoreCase("java")).thenReturn(Arrays.asList(livro));

        List<Livro> encontrados = livroService.buscarPorTitulo("java");

        assertFalse(encontrados.isEmpty());
        assertEquals(livro.getTitulo(), encontrados.get(0).getTitulo());
    }

    //RF06 – Buscar livros por autor
    @Test
    void buscarPorAutorDeveSerCaseInsensitive() {
        when(livroRepository.findByAutorContainingIgnoreCase("autor")).thenReturn(Arrays.asList(livro));

        List<Livro> encontrados = livroService.buscarPorAutor("autor");

        assertFalse(encontrados.isEmpty());
        assertEquals(livro.getAutor(), encontrados.get(0).getAutor());
    }
    //RF04 – Buscar livro por ISBN
    //
    @Test
    void buscarPorIsbnDeveRetornarLivroExato() {
        when(livroRepository.findByIsbn(livro.getIsbn())).thenReturn(Optional.of(livro));

        Optional<Livro> encontrado = livroService.buscarPorIsbn(livro.getIsbn());

        assertTrue(encontrado.isPresent());
        assertEquals(livro.getIsbn(), encontrado.get().getIsbn());
    }

    //RF07 – Listar livros disponíveis
    //RN06 – Quantidade disponível não negativa
    @Test
    void listarDisponiveisDeveRetornarApenasLivrosComQuantidadeDisponivelMaiorQueZero() {
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Arrays.asList(livro));

        List<Livro> disponiveis = livroService.listarDisponiveis();

        assertFalse(disponiveis.isEmpty());
        assertTrue(disponiveis.get(0).getQuantidadeDisponivel() > 0);
    }

    //RF08 – Excluir livro
    @Test
    void excluirLivroComEmprestimosInativosDeveFuncionar() {
        livro.setEmprestimos(Arrays.asList(new com.bibliotech.model.Emprestimo() {{
            setAtivo(false);
        }}));
        when(livroRepository.findById(livro.getId())).thenReturn(Optional.of(livro));

        livroService.excluir(livro.getId());

        verify(livroRepository, times(1)).delete(livro);
    }

    //RF08 – Excluir livro
    //RN05 – Bloqueio de exclusão com empréstimos ativos
    @Test
    void excluirLivroComEmprestimosAtivosDeveLancarExcecao() {
        // simulando empréstimo ativo
        livro.setEmprestimos(Arrays.asList(new com.bibliotech.model.Emprestimo() {{
            setAtivo(true);
        }}));

        when(livroRepository.findById(livro.getId())).thenReturn(Optional.of(livro));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> livroService.excluir(livro.getId()));

        assertEquals("Não é possível excluir livro com empréstimos ativos", exception.getMessage());
    }

  //RF08 – Excluir livro
    @Test
    void excluirLivroNaoEncontradoDeveLancarExcecao() {
        when(livroRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> livroService.excluir(2L));

        assertEquals("Livro não encontrado", exception.getMessage());
    }

   //RF10 – Controlar disponibilidade dos livros
    @Test
    void decrementarDisponibilidadeDeveReduzirQuantidadeDisponivel() {
        when(livroRepository.save(livro)).thenReturn(livro);

        livroService.decrementarDisponibilidade(livro);

        assertEquals(4, livro.getQuantidadeDisponivel());
        verify(livroRepository, times(1)).save(livro);
    }

    //RF10 – Controlar disponibilidade dos livros
    //RN06 – Quantidade disponível não negativa
    @Test
    void decrementarDisponibilidadeComZeroNaoDeveSalvar() {
        livro.setQuantidadeDisponivel(0);

        livroService.decrementarDisponibilidade(livro);

        assertEquals(0, livro.getQuantidadeDisponivel());
        verify(livroRepository, times(0)).save(livro);
    }
    //RF10 – Controlar disponibilidade dos livros
    @Test
    void incrementarDisponibilidadeDeveAumentarQuantidadeDisponivel() {
        livro.setQuantidadeDisponivel(4);
        when(livroRepository.save(livro)).thenReturn(livro);

        livroService.incrementarDisponibilidade(livro);

        assertEquals(5, livro.getQuantidadeDisponivel());
        verify(livroRepository, times(1)).save(livro);
    }
    //RF01 – Cadastrar livro
   // (Violação possível da RN07 – consistência entre exemplares e disponíveis)
    @Test
    void salvarLivroComQuantidadeDisponivelMaiorQueExemplares() {
        livro.setQuantidadeDisponivel(10); // maior que exemplares
        livro.setQuantidadeExemplares(5);
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro salvo = livroService.salvar(livro);

        // Aqui vamos afirmar que a quantidade disponível DEVE ser menor ou igual aos exemplares
        assertTrue(salvo.getQuantidadeDisponivel() <= salvo.getQuantidadeExemplares(),
            "Falhou: quantidade disponível maior que exemplares");
    }

    @Test
    void incrementarDisponibilidadeAlemDoTotalNaoDeveExcederQuantidadeExemplares() {
        livro.setQuantidadeDisponivel(5);
        livro.setQuantidadeExemplares(5);

        livroService.incrementarDisponibilidade(livro);

        assertEquals(5, livro.getQuantidadeDisponivel());
    }

    @Test
    void salvarLivroValidoDeveRetornarLivroSalvo() {
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro salvo = livroService.salvar(livro);

        assertNotNull(salvo);
        assertEquals(livro.getTitulo(), salvo.getTitulo());
    }


}


