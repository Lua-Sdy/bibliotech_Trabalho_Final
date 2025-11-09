package com.bibliotech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um livro no sistema.
 * 
 * <p>Armazena informações bibliográficas do livro e controla a
 * quantidade de exemplares disponíveis para empréstimo.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    @Column(nullable = false)
    private String autor;

    @NotBlank(message = "ISBN é obrigatório")
    @Column(nullable = false)
    private String isbn;

    private String editora;

    @Min(value = 1000, message = "Ano deve ser maior que 1000")
    @Max(value = 2100, message = "Ano deve ser menor que 2100")
    private Integer ano;

    @Min(value = 1, message = "Quantidade de exemplares deve ser maior que 0")
    @Column(nullable = false)
    private Integer quantidadeExemplares;

    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL)
    private List<Emprestimo> emprestimos = new ArrayList<>();

    /**
     * Construtor padrão.
     */
    public Livro() {
    }

    /**
     * Construtor com parâmetros para facilitar a criação de livros.
     * 
     * @param titulo título do livro
     * @param autor autor do livro
     * @param isbn código ISBN
     * @param editora editora do livro
     * @param ano ano de publicação
     * @param quantidadeExemplares quantidade total de exemplares
     */
    public Livro(String titulo, String autor, String isbn, String editora, 
                 Integer ano, Integer quantidadeExemplares) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editora = editora;
        this.ano = ano;
        this.quantidadeExemplares = quantidadeExemplares;
        this.quantidadeDisponivel = quantidadeExemplares;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public void setQuantidadeExemplares(Integer quantidadeExemplares) {
        this.quantidadeExemplares = quantidadeExemplares;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public List<Emprestimo> getEmprestimos() {
        return emprestimos;
    }

    public void setEmprestimos(List<Emprestimo> emprestimos) {
        this.emprestimos = emprestimos;
    }

    /**
     * Verifica se o livro possui exemplares disponíveis para empréstimo.
     * 
     * @return true se quantidade disponível for maior que zero, false caso contrário
     */
    public boolean isDisponivel() {
        return quantidadeDisponivel > 0;
    }
}
