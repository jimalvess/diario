package com.diario.controller;

import com.diario.service.PasswordResetService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService service;

    @PostMapping("/esqueci-senha")
    public ResponseEntity<String> esqueciSenha(@RequestBody EmailRequest request) {
        service.solicitarResetDeSenha(request.getEmail());
        return ResponseEntity.ok("Link de redefinição de senha enviado para seu email");
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<String> redefinirSenha(@RequestBody ResetSenhaRequest request) {
        try {
            service.redefinirSenha(request.getToken(), request.getNovaSenha());
            return ResponseEntity.ok("Senha redefinida com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Data
    public static class EmailRequest {
        private String email;
    }

    @Data
    public static class ResetSenhaRequest {
        private String token;
        private String novaSenha;
    }
}
