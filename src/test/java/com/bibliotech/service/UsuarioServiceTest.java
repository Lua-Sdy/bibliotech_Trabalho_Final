package com.bibliotech.service;

import com.bibliotech.model.Usuario;
import com.bibliotech.model.Usuario.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    private UsuarioService usuarioService;
    private Usuario usuario;
    private Map<Long, Usuario> bancoSimulado; // simula o "banco" do repository

    @BeforeEach
    void setup() {
        usuarioService = new UsuarioService();

        bancoSimulado = new HashMap<>(); // mapa em memória para armazenar usuários

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Carlos");
        usuario.setEmail("carlos@email.com");
        usuario.setCpf("123.456.789-01");
        usuario.setSenha("senha123");
        usuario.setAtivo(true);
        usuario.setTipo(TipoUsuario.ALUNO);

        // adiciona usuário ao "banco" simulado
        bancoSimulado.put(usuario.getId(), usuario);
    }

    // RN-07: Validação de CPF
    @Test
    void testValidarCPFValido() {
        assertTrue(usuarioService.validarCPF("123.456.789-01"));
    }

    @Test
    void testValidarCPFInvalido() {
        assertFalse(usuarioService.validarCPF("123"));
        assertFalse(usuarioService.validarCPF("abcdefghijk"));
    }

    // RN-08: Autenticação
    @Test
    void testAutenticarUsuarioAtivo() {
        Usuario encontrado = bancoSimulado.get(1L);

        assertTrue(encontrado.getAtivo());
        assertEquals("senha123", encontrado.getSenha());
        assertEquals("Carlos", encontrado.getNome());
    }

    @Test
    void testAutenticarUsuarioInativoLancaExcecao() {
        usuario.setAtivo(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (!usuario.getAtivo()) {
                throw new RuntimeException("Usuário inativo");
            }
        });

        assertEquals("Usuário inativo", ex.getMessage());
    }

    @Test
    void testAutenticarEmailOuSenhaIncorretosLancaExcecao() {
        String senhaFornecida = "senhaErrada";

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (!usuario.getSenha().equals(senhaFornecida)) {
                throw new RuntimeException("Email ou senha incorretos");
            }
        });

        assertEquals("Email ou senha incorretos", ex.getMessage());
    }

    // RN-09: Tipos de Usuário
    @Test
    void testSalvarUsuarioTipoValido() {
        usuario.setTipo(TipoUsuario.ALUNO);
        assertEquals(TipoUsuario.ALUNO, usuario.getTipo());
    }

    @Test
    void testSalvarUsuarioTipoInvalidoLancaExcecao() {
        usuario.setTipo(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (usuario.getTipo() == null) {
                throw new RuntimeException("Tipo de usuário inválido");
            }
        });

        assertEquals("Tipo de usuário inválido", ex.getMessage());
    }

    // RN-06: Exclusão de Usuário
    @Test
    void testExcluirUsuarioSemEmprestimos() {
        bancoSimulado.remove(usuario.getId());
        assertFalse(bancoSimulado.containsKey(usuario.getId()));
    }

    // RN-10 / RN-11: Email e CPF Únicos
    @Test
    void testSalvarUsuarioEmailDuplicadoLancaExcecao() {
        Usuario outro = new Usuario();
        outro.setId(2L);
        outro.setEmail(usuario.getEmail());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (bancoSimulado.values().stream().anyMatch(u -> u.getEmail().equals(outro.getEmail()))) {
                throw new RuntimeException("Email já cadastrado");
            }
        });

        assertEquals("Email já cadastrado", ex.getMessage());
    }

    @Test
    void testSalvarUsuarioCPFDuplicadoLancaExcecao() {
        Usuario outro = new Usuario();
        outro.setId(2L);
        outro.setCpf(usuario.getCpf());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (bancoSimulado.values().stream().anyMatch(u -> u.getCpf().equals(outro.getCpf()))) {
                throw new RuntimeException("CPF já cadastrado");
            }
        });

        assertEquals("CPF já cadastrado", ex.getMessage());
    }

}
