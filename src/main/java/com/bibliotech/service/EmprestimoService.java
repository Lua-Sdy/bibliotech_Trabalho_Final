package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela lógica de negócio relacionada aos empréstimos.
 * 
 * <p>Gerencia todo o ciclo de vida de um empréstimo, desde sua criação
 * até a devolução, incluindo cálculo de prazos, multas por atraso e
 * controle de disponibilidade de livros.</p>
 * 
 * <p><strong>Regras de Negócio:</strong></p>
 * <ul>
 *   <li>Prazo padrão de devolução: 14 dias corridos</li>
 *   <li>Multa por atraso: R$ 2,00 por dia</li>
 *   <li>Apenas livros disponíveis podem ser emprestados</li>
 * </ul>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Service
public class EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private LivroService livroService;

    /**
     * Realiza um novo empréstimo de livro para um usuário.
     * 
     * <p>Cria um registro de empréstimo com a data atual como data de
     * empréstimo e calcula automaticamente a data de devolução prevista.
     * Após criar o empréstimo, decrementa a quantidade disponível do livro.</p>
     * 
     * @param usuario o usuário que está realizando o empréstimo
     * @param livro o livro a ser emprestado
     * @return o empréstimo criado e salvo no banco de dados
     * @throws RuntimeException se o livro não estiver disponível
     */
    public Emprestimo realizarEmprestimo(Usuario usuario, Livro livro) {
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataDevolucao = calcularDataDevolucao(dataEmprestimo);

        Emprestimo emprestimo = new Emprestimo(
            usuario, livro, dataEmprestimo, dataDevolucao);
        emprestimo = emprestimoRepository.save(emprestimo);

        livroService.decrementarDisponibilidade(livro);

        return emprestimo;
    }

    /**
     * Calcula a data de devolução prevista baseada na data de empréstimo.
     * 
     * <p>De acordo com a regra de negócio RN-01, o prazo padrão de
     * devolução é de 14 dias corridos a partir da data do empréstimo.</p>
     * 
     * @param dataEmprestimo a data em que o empréstimo foi realizado
     * @return a data prevista para devolução (14 dias após o empréstimo)
     */
    public LocalDate calcularDataDevolucao(LocalDate dataEmprestimo) {
        return dataEmprestimo.plusDays(7);
    }

    /**
     * Calcula o valor da multa por atraso na devolução.
     * 
     * <p>Conforme a regra de negócio RN-03, a multa é de R$ 2,00 por dia
     * de atraso. O cálculo é feito com base na diferença entre a data
     * prevista e a data real de devolução (ou data atual se ainda ativo).</p>
     * 
     * @param emprestimo o empréstimo para o qual calcular a multa
     * @return o valor da multa em reais (0.0 se não houver atraso)
     */
    public double calcularMulta(Emprestimo emprestimo) {
        if (emprestimo.getDataDevolucaoReal() == null) {
            if (LocalDate.now().isAfter(emprestimo.getDataDevolucaoPrevista())) {
                long diasAtraso = ChronoUnit.DAYS.between(
                    emprestimo.getDataDevolucaoPrevista(), 
                    LocalDate.now()
                );
                return diasAtraso * 3.0;
            }
        } else {
            if (emprestimo.getDataDevolucaoReal()
                    .isAfter(emprestimo.getDataDevolucaoPrevista())) {
                long diasAtraso = ChronoUnit.DAYS.between(
                    emprestimo.getDataDevolucaoPrevista(), 
                    emprestimo.getDataDevolucaoReal()
                );
                return diasAtraso * 3.0;
            }
        }
        return 0.0;
    }

    /**
     * Registra a devolução de um livro emprestado.
     * 
     * <p>Define a data de devolução real como a data atual, calcula
     * a multa (se houver), marca o empréstimo como inativo e incrementa
     * a quantidade disponível do livro.</p>
     * 
     * @param emprestimoId o identificador do empréstimo
     * @return o empréstimo atualizado com os dados da devolução
     * @throws RuntimeException se o empréstimo não for encontrado ou
     *         se já tiver sido devolvido anteriormente
     */
    public Emprestimo registrarDevolucao(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
            .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        if (emprestimo.getDataDevolucaoReal() != null) {
            throw new RuntimeException("Empréstimo já foi devolvido");
        }

        emprestimo.setDataDevolucaoReal(LocalDate.now());
        emprestimo.setAtivo(false);

        double multa = calcularMulta(emprestimo);
        emprestimo.setMulta(multa);

        livroService.incrementarDisponibilidade(emprestimo.getLivro());

        return emprestimoRepository.save(emprestimo);
    }

    /**
     * Retorna todos os empréstimos cadastrados no sistema.
     * 
     * @return lista com todos os empréstimos
     */
    public List<Emprestimo> listarTodos() {
        return emprestimoRepository.findAll();
    }

    /**
     * Retorna apenas os empréstimos ativos (não devolvidos).
     * 
     * @return lista de empréstimos ativos
     */
    public List<Emprestimo> listarAtivos() {
        return emprestimoRepository.findByAtivoTrue();
    }

    /**
     * Retorna todos os empréstimos de um usuário específico.
     * 
     * @param usuario o usuário cujos empréstimos serão listados
     * @return lista de empréstimos do usuário
     */
    public List<Emprestimo> listarPorUsuario(Usuario usuario) {
        return emprestimoRepository.findByUsuario(usuario);
    }

    /**
     * Retorna apenas os empréstimos ativos de um usuário específico.
     * 
     * @param usuario o usuário cujos empréstimos ativos serão listados
     * @return lista de empréstimos ativos do usuário
     */
    public List<Emprestimo> listarAtivosPorUsuario(Usuario usuario) {
        return emprestimoRepository.findByUsuarioAndAtivoTrue(usuario);
    }

    /**
     * Retorna todos os empréstimos que estão atrasados.
     * 
     * <p>Um empréstimo é considerado atrasado quando está ativo e
     * a data de devolução prevista é anterior à data atual.</p>
     * 
     * @return lista de empréstimos atrasados
     */
    public List<Emprestimo> listarAtrasados() {
        return emprestimoRepository.findEmprestimosAtrasados();
    }

    /**
     * Busca um empréstimo específico pelo seu identificador único.
     * 
     * @param id o identificador do empréstimo
     * @return Optional contendo o empréstimo se encontrado, vazio caso contrário
     */
    public Optional<Emprestimo> buscarPorId(Long id) {
        return emprestimoRepository.findById(id);
    }
}
