package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por fornecer dados estatísticos para o dashboard.
 * 
 * <p>Consolida informações de diferentes entidades do sistema para
 * apresentar uma visão geral das operações da biblioteca, incluindo
 * totais de livros, usuários, empréstimos e outras métricas relevantes.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Service
public class DashboardService {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    /**
     * Obtém as estatísticas gerais do sistema para exibição no dashboard.
     * 
     * <p>Retorna um mapa contendo as seguintes informações:</p>
     * <ul>
     *   <li><strong>totalLivros:</strong> quantidade total de livros cadastrados</li>
     *   <li><strong>totalUsuarios:</strong> quantidade de usuários ativos</li>
     *   <li><strong>emprestimosAtivos:</strong> quantidade de empréstimos em aberto</li>
     *   <li><strong>livrosDisponiveis:</strong> quantidade de livros com exemplares disponíveis</li>
     *   <li><strong>emprestimosAtrasados:</strong> quantidade de empréstimos vencidos</li>
     * </ul>
     * 
     * @return mapa contendo as estatísticas do sistema
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> estatisticas = new HashMap<>();

        long totalLivros = livroRepository.count();
        estatisticas.put("totalLivros", totalLivros);

        long totalUsuarios = usuarioRepository.findByAtivoTrue().size();
        estatisticas.put("totalUsuarios", totalUsuarios);

        long emprestimosAtivos = emprestimoRepository.count();
        estatisticas.put("emprestimosAtivos", emprestimosAtivos);

        long livrosDisponiveis = 
            livroRepository.findByQuantidadeDisponivelGreaterThan(0).size();
        estatisticas.put("livrosDisponiveis", livrosDisponiveis);

        long emprestimosAtrasados = 
            emprestimoRepository.findEmprestimosAtrasados().size();
        estatisticas.put("emprestimosAtrasados", emprestimosAtrasados);

        return estatisticas;
    }

    /**
     * Obtém a lista dos livros mais emprestados do sistema.
     * 
     * <p>Retorna uma lista ordenada decrescente por quantidade de empréstimos,
     * útil para análise de popularidade dos livros.</p>
     * 
     * @return lista de arrays onde cada array contém [Livro, quantidade]
     */
    public List<Object[]> obterLivrosMaisEmprestados() {
        return emprestimoRepository.findLivrosMaisEmprestados();
    }

    /**
     * Obtém a lista de todos os empréstimos atrasados.
     * 
     * <p>Útil para gestão de cobranças e acompanhamento de devoluções pendentes.</p>
     * 
     * @return lista de empréstimos com devolução em atraso
     */
    public List<Emprestimo> obterEmprestimosAtrasados() {
        return emprestimoRepository.findEmprestimosAtrasados();
    }
}
