package com.bibliotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BiblioTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiblioTechApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🚀 BiblioTech iniciado com sucesso!");
        System.out.println("📚 Acesse: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
