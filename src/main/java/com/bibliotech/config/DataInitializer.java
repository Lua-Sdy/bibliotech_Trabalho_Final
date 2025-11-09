package com.bibliotech.config;

import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.model.Emprestimo;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import com.bibliotech.repository.EmprestimoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Criar usuários de teste
        Usuario admin = new Usuario("Administrador", "admin@bibliotech.com", 
            "000.000.000-00", "admin123", Usuario.TipoUsuario.FUNCIONARIO);

        Usuario aluno1 = new Usuario("João Silva", "joao@email.com",
            "111.111.111-11", "senha123", Usuario.TipoUsuario.ALUNO);

        Usuario aluno2 = new Usuario("Maria Santos", "maria@email.com",
            "222.222.222-22", "senha123", Usuario.TipoUsuario.ALUNO);

        Usuario professor = new Usuario("Prof. Carlos", "carlos@email.com",
            "333.333.333-33", "senha123", Usuario.TipoUsuario.PROFESSOR);

        usuarioRepository.save(admin);
        usuarioRepository.save(aluno1);
        usuarioRepository.save(aluno2);
        usuarioRepository.save(professor);

        // Criar livros de teste
        Livro livro1 = new Livro("Clean Code", "Robert C. Martin", 
            "978-0132350884", "Prentice Hall", 2008, 3);

        Livro livro2 = new Livro("Design Patterns", "Gang of Four",
            "978-0201633610", "Addison-Wesley", 1994, 2);

        Livro livro3 = new Livro("Refactoring", "Martin Fowler",
            "978-0134757599", "Addison-Wesley", 2018, 4);

        Livro livro4 = new Livro("Test Driven Development", "Kent Beck",
            "978-0321146533", "Addison-Wesley", 2002, 2);

        Livro livro5 = new Livro("The Pragmatic Programmer", "Hunt & Thomas",
            "978-0135957059", "Addison-Wesley", 2019, 3);

        livroRepository.save(livro1);
        livroRepository.save(livro2);
        livroRepository.save(livro3);
        livroRepository.save(livro4);
        livroRepository.save(livro5);

        // Criar alguns empréstimos de exemplo
        Emprestimo emp1 = new Emprestimo(aluno1, livro1, 
            LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));
        livro1.setQuantidadeDisponivel(livro1.getQuantidadeDisponivel() - 1);

        Emprestimo emp2 = new Emprestimo(aluno2, livro2,
            LocalDate.now().minusDays(3), LocalDate.now().plusDays(11));
        livro2.setQuantidadeDisponivel(livro2.getQuantidadeDisponivel() - 1);

        // Criar um empréstimo já devolvido (para histórico)
        Emprestimo emp3 = new Emprestimo(professor, livro3,
            LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));
        emp3.setDataDevolucaoReal(LocalDate.now().minusDays(5));
        emp3.setAtivo(false);
        emp3.setMulta(0.0);

        emprestimoRepository.save(emp1);
        emprestimoRepository.save(emp2);
        emprestimoRepository.save(emp3);

        livroRepository.save(livro1);
        livroRepository.save(livro2);

        System.out.println("✅ Dados de exemplo carregados!");
        System.out.println("📌 Login: admin@bibliotech.com / admin123");
    }
}
