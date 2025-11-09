package com.bibliotech.controller;

import com.bibliotech.model.Livro;
import com.bibliotech.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca,
                        @RequestParam(required = false) String tipo,
                        Model model) {
        List<Livro> livros;

        if (busca != null && !busca.isEmpty()) {
            if ("titulo".equals(tipo)) {
                livros = livroService.buscarPorTitulo(busca);
            } else if ("autor".equals(tipo)) {
                livros = livroService.buscarPorAutor(busca);
            } else {
                livros = livroService.listarTodos();
            }
        } else {
            livros = livroService.listarTodos();
        }

        model.addAttribute("livros", livros);
        model.addAttribute("busca", busca);
        model.addAttribute("tipo", tipo);

        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("livro", new Livro());
        return "livros/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return livroService.buscarPorId(id)
            .map(livro -> {
                model.addAttribute("livro", livro);
                return "livros/form";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute("erro", "Livro não encontrado");
                return "redirect:/livros";
            });
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Livro livro,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "livros/form";
        }

        try {
            livroService.salvar(livro);
            redirectAttributes.addFlashAttribute("sucesso", "Livro salvo com sucesso!");
            return "redirect:/livros";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/livros/novo";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            livroService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Livro excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/livros";
    }
}
