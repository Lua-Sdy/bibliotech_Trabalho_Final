package com.bibliotech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um usuário do sistema.
 * 
 * <p>Usuários podem ser de três tipos: ALUNO, PROFESSOR ou FUNCIONARIO.
 * Cada tipo pode ter permissões e regras específicas.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Column(nullable = false, unique = true)
    private String cpf;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @NotNull(message = "Tipo de usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Emprestimo> emprestimos = new ArrayList<>();

    /**
     * Enum que define os tipos possíveis de usuários no sistema.
     */
    public enum TipoUsuario {
        ALUNO("Aluno"),
        PROFESSOR("Professor"),
        FUNCIONARIO("Funcionário");

        private String descricao;

        TipoUsuario(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public Usuario() {
    }

    public Usuario(String nome, String email, String cpf, String senha, TipoUsuario tipo) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.tipo = tipo;
        this.ativo = true;
    }

    // Getters e Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public List<Emprestimo> getEmprestimos() { return emprestimos; }
    public void setEmprestimos(List<Emprestimo> e) { this.emprestimos = e; }

    /**
     * Verifica se o usuário possui empréstimos ativos.
     * 
     * @return true se houver pelo menos um empréstimo ativo, false caso contrário
     */
    public boolean temEmprestimosAtivos() {
        return emprestimos.stream()
            .anyMatch(e -> e.getDataDevolucaoReal() == null);
    }
}
