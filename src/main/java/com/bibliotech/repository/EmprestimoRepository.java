package com.bibliotech.repository;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Usuario;
import com.bibliotech.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByAtivoTrue();

    List<Emprestimo> findByUsuario(Usuario usuario);

    List<Emprestimo> findByUsuarioAndAtivoTrue(Usuario usuario);

    List<Emprestimo> findByLivro(Livro livro);

    @Query("SELECT e FROM Emprestimo e WHERE e.ativo = true AND e.dataDevolucaoPrevista < CURRENT_DATE")
    List<Emprestimo> findEmprestimosAtrasados();

    @Query("SELECT e.livro, COUNT(e) as total FROM Emprestimo e GROUP BY e.livro ORDER BY total DESC")
    List<Object[]> findLivrosMaisEmprestados();
}
