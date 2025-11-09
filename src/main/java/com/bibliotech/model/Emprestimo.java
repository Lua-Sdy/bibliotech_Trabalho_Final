package com.bibliotech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidade que representa um empréstimo de livro.
 * 
 * <p>Registra toda a transação de empréstimo, incluindo datas,
 * valores de multa e status de ativo/devolvido.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @NotNull
    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @NotNull
    @Column(nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column
    private LocalDate dataDevolucaoReal;

    @Column
    private Double multa = 0.0;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Emprestimo() {
    }

    public Emprestimo(Usuario usuario, Livro livro, LocalDate dataEmprestimo, 
                      LocalDate dataDevolucaoPrevista) {
        this.usuario = usuario;
        this.livro = livro;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.ativo = true;
    }

    // Getters e Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDate d) { this.dataEmprestimo = d; }

    public LocalDate getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDate d) { this.dataDevolucaoPrevista = d; }

    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate d) { this.dataDevolucaoReal = d; }

    public Double getMulta() { return multa; }
    public void setMulta(Double multa) { this.multa = multa; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    /**
     * Verifica se o empréstimo está atrasado.
     * 
     * @return true se ainda não foi devolvido e a data prevista já passou
     */
    public boolean isAtrasado() {
        if (dataDevolucaoReal != null) return false;
        return LocalDate.now().isAfter(dataDevolucaoPrevista);
    }

    /**
     * Calcula quantos dias de atraso o empréstimo possui.
     * 
     * @return quantidade de dias de atraso (0 se não estiver atrasado)
     */
    public long getDiasAtraso() {
        if (!isAtrasado()) return 0;
        return ChronoUnit.DAYS.between(dataDevolucaoPrevista, LocalDate.now());
    }
}
