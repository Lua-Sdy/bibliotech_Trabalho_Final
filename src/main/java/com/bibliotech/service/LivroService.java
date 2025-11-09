package com.bibliotech.service;

import com.bibliotech.model.Livro;
import com.bibliotech.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela lógica de negócio relacionada aos livros.
 * 
 * <p>Gerencia operações CRUD (Create, Read, Update, Delete) de livros,
 * incluindo validações de regras de negócio, controle de disponibilidade
 * e busca por diferentes critérios.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Service
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;

    /**
     * Salva um novo livro ou atualiza um existente no sistema.
     * 
     * <p>Se for um livro novo (ID null), a quantidade disponível é 
     * automaticamente definida igual à quantidade de exemplares.
     * Para livros existentes, apenas atualiza as informações.</p>
     * 
     * @param livro o livro a ser salvo
     * @return o livro salvo com ID atribuído
     * @throws RuntimeException se houver erro ao salvar
     */
    public Livro salvar(Livro livro) {
        if (livro.getQuantidadeDisponivel() == null) {
            livro.setQuantidadeDisponivel(livro.getQuantidadeExemplares());
        }

        return livroRepository.save(livro);
    }

    /**
     * Retorna todos os livros cadastrados no sistema.
     * 
     * @return lista com todos os livros
     */
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    /**
     * Busca um livro específico pelo seu identificador único.
     * 
     * @param id o identificador do livro
     * @return Optional contendo o livro se encontrado, vazio caso contrário
     */
    public Optional<Livro> buscarPorId(Long id) {
        return livroRepository.findById(id);
    }

    /**
     * Busca um livro pelo seu código ISBN.
     * 
     * @param isbn o código ISBN do livro
     * @return Optional contendo o livro se encontrado, vazio caso contrário
     */
    public Optional<Livro> buscarPorIsbn(String isbn) {
        return livroRepository.findByIsbn(isbn);
    }

    /**
     * Busca livros cujo título contenha o texto fornecido.
     * A busca não é case-sensitive.
     * 
     * @param titulo o texto a ser buscado no título
     * @return lista de livros encontrados
     */
    public List<Livro> buscarPorTitulo(String titulo) {
        return livroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    /**
     * Busca livros cujo autor contenha o texto fornecido.
     * A busca não é case-sensitive.
     * 
     * @param autor o texto a ser buscado no nome do autor
     * @return lista de livros encontrados
     */
    public List<Livro> buscarPorAutor(String autor) {
        return livroRepository.findByAutorContainingIgnoreCase(autor);
    }

    /**
     * Retorna apenas os livros que possuem exemplares disponíveis
     * para empréstimo (quantidade disponível maior que zero).
     * 
     * @return lista de livros disponíveis
     */
    public List<Livro> listarDisponiveis() {
        return livroRepository.findByQuantidadeDisponivelGreaterThan(0);
    }

    /**
     * Exclui um livro do sistema.
     * 
     * <p>A exclusão só é permitida se o livro não possuir empréstimos
     * ativos. Caso contrário, uma exceção é lançada.</p>
     * 
     * @param id o identificador do livro a ser excluído
     * @throws RuntimeException se o livro não for encontrado ou
     *         se houver empréstimos ativos
     */
    public void excluir(Long id) {
        Livro livro = livroRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        boolean temEmprestimosAtivos = livro.getEmprestimos().stream()
            .anyMatch(e -> e.getAtivo());

        if (temEmprestimosAtivos) {
            throw new RuntimeException(
                "Não é possível excluir livro com empréstimos ativos");
        }

        livroRepository.delete(livro);
    }

    /**
     * Incrementa a quantidade de exemplares disponíveis do livro.
     * Utilizado quando um livro é devolvido.
     * 
     * @param livro o livro a ter disponibilidade incrementada
     */
    public void incrementarDisponibilidade(Livro livro) {
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() + 1);
        livroRepository.save(livro);
    }

    /**
     * Decrementa a quantidade de exemplares disponíveis do livro.
     * Utilizado quando um livro é emprestado.
     * 
     * @param livro o livro a ter disponibilidade decrementada
     */
    public void decrementarDisponibilidade(Livro livro) {
        if (livro.getQuantidadeDisponivel() > 0) {
            livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);
            livroRepository.save(livro);
        }
    }
}
