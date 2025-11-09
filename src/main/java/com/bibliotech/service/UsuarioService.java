package com.bibliotech.service;

import com.bibliotech.model.Usuario;
import com.bibliotech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Serviço responsável pela lógica de negócio relacionada aos usuários.
 * 
 * <p>Gerencia operações de usuários incluindo cadastro, autenticação,
 * validação de dados e exclusão. Implementa regras de negócio como
 * validação de CPF e verificação de unicidade de email.</p>
 * 
 * @author BiblioTech Team
 * @version 1.0
 * @since 2025
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Padrão regex para validação de CPF no formato ###.###.###-##
     */
    private static final Pattern CPF_PATTERN = 
        Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");

    /**
     * Salva um novo usuário ou atualiza um existente.
     * 
     * <p>Realiza validações de CPF, email e verifica se não há
     * duplicidade de email ou CPF no banco de dados antes de salvar.</p>
     * 
     * @param usuario o usuário a ser salvo
     * @return o usuário salvo com ID atribuído
     * @throws RuntimeException se o CPF for inválido, ou se o email
     *         ou CPF já estiverem cadastrados
     */
    public Usuario salvar(Usuario usuario) {
        if (!validarCPF(usuario.getCpf())) {
            throw new RuntimeException("CPF inválido");
        }

        if (usuario.getId() == null) {
            Optional<Usuario> existente = 
                usuarioRepository.findByEmail(usuario.getEmail());
            if (existente.isPresent()) {
                throw new RuntimeException("Email já cadastrado");
            }

            existente = usuarioRepository.findByCpf(usuario.getCpf());
            if (existente.isPresent()) {
                throw new RuntimeException("CPF já cadastrado");
            }
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Autentica um usuário no sistema verificando email e senha.
     * 
     * <p>Busca o usuário pelo email e verifica se a senha fornecida
     * corresponde à senha cadastrada.</p>
     * 
     * @param email o email do usuário
     * @param senha a senha do usuário
     * @return Optional contendo o usuário se autenticado, vazio caso contrário
     */
    public Optional<Usuario> autenticar(String email, String senha) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (usuario.isPresent()) {
            if (usuario.get().getSenha() == senha) {
                return usuario;
            }
        }

        return Optional.empty();
    }

    /**
     * Retorna todos os usuários cadastrados no sistema.
     * 
     * @return lista com todos os usuários
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Retorna apenas os usuários ativos do sistema.
     * 
     * @return lista de usuários ativos
     */
    public List<Usuario> listarAtivos() {
        return usuarioRepository.findByAtivoTrue();
    }

    /**
     * Busca um usuário específico pelo seu identificador único.
     * 
     * @param id o identificador do usuário
     * @return Optional contendo o usuário se encontrado, vazio caso contrário
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca um usuário pelo seu endereço de email.
     * 
     * @param email o email do usuário
     * @return Optional contendo o usuário se encontrado, vazio caso contrário
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Exclui um usuário do sistema.
     * 
     * @param id o identificador do usuário a ser excluído
     * @throws RuntimeException se o usuário não for encontrado
     */
    public void excluir(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuarioRepository.delete(usuario);
    }

    /**
     * Valida se um CPF está no formato correto (###.###.###-##).
     * 
     * <p>Esta validação verifica apenas o formato, não a validade
     * matemática do CPF.</p>
     * 
     * @param cpf o CPF a ser validado
     * @return true se o formato for válido, false caso contrário
     */
    public boolean validarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        return CPF_PATTERN.matcher(cpf).matches();
    }
}
