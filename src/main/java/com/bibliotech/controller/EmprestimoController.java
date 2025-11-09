package com.bibliotech.controller;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.service.EmprestimoService;
import com.bibliotech.service.LivroService;
import com.bibliotech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoController {

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private LivroService livroService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro, Model model) {
        List<Emprestimo> emprestimos;

        if ("ativos".equals(filtro)) {
            emprestimos = emprestimoService.listarAtivos();
        } else if ("atrasados".equals(filtro)) {
            emprestimos = emprestimoService.listarAtrasados();
        } else {
            emprestimos = emprestimoService.listarTodos();
        }

        model.addAttribute("emprestimos", emprestimos);
        model.addAttribute("filtro", filtro);

        return "emprestimos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        List<Usuario> usuarios = usuarioService.listarAtivos();
        List<Livro> livros = livroService.listarDisponiveis();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("livros", livros);

        return "emprestimos/form";
    }

    @PostMapping("/realizar")
    public String realizar(@RequestParam Long usuarioId,
                          @RequestParam Long livroId,
                          RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Livro livro = livroService.buscarPorId(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

            emprestimoService.realizarEmprestimo(usuario, livro);
            redirectAttributes.addFlashAttribute("sucesso", "Empréstimo realizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/emprestimos";
    }

    @GetMapping("/devolver/{id}")
    public String devolver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Emprestimo emprestimo = emprestimoService.registrarDevolucao(id);

            String mensagem = "Devolução registrada com sucesso!";
            if (emprestimo.getMulta() > 0) {
                mensagem += " Multa: R$ " + String.format("%.2f", emprestimo.getMulta());
            }

            redirectAttributes.addFlashAttribute("sucesso", mensagem);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/emprestimos";
    }

    @GetMapping("/usuario/{usuarioId}")
    public String listarPorUsuario(@PathVariable Long usuarioId, Model model, RedirectAttributes redirectAttributes) {
        return usuarioService.buscarPorId(usuarioId)
            .map(usuario -> {
                List<Emprestimo> emprestimos = emprestimoService.listarPorUsuario(usuario);
                model.addAttribute("usuario", usuario);
                model.addAttribute("emprestimos", emprestimos);
                return "emprestimos/historico";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado");
                return "redirect:/usuarios";
            });
    }
}
