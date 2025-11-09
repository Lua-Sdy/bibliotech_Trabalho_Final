package com.bibliotech.controller;

import com.bibliotech.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();

        model.addAttribute("totalLivros", estatisticas.get("totalLivros"));
        model.addAttribute("totalUsuarios", estatisticas.get("totalUsuarios"));
        model.addAttribute("emprestimosAtivos", estatisticas.get("emprestimosAtivos"));
        model.addAttribute("livrosDisponiveis", estatisticas.get("livrosDisponiveis"));
        model.addAttribute("emprestimosAtrasados", estatisticas.get("emprestimosAtrasados"));

        return "dashboard";
    }
}
