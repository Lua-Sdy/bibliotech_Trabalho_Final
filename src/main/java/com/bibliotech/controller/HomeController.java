package com.bibliotech.controller;

import com.bibliotech.model.Usuario;
import com.bibliotech.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String senha,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuario = usuarioService.autenticar(email, senha);

        if (usuario.isPresent() && usuario.get().getAtivo()) {
            session.setAttribute("usuarioLogado", usuario.get());
            return "redirect:/dashboard";
        }

        redirectAttributes.addFlashAttribute("erro", "Email ou senha inválidos");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
