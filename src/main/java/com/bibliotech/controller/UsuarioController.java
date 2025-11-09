package com.bibliotech.controller;

import com.bibliotech.model.Usuario;
import com.bibliotech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return usuarioService.buscarPorId(id)
            .map(usuario -> {
                model.addAttribute("usuario", usuario);
                model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
                return "usuarios/form";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado");
                return "redirect:/usuarios";
            });
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
            return "usuarios/form";
        }

        try {
            usuarioService.salvar(usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário salvo com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
            return "usuarios/form";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
