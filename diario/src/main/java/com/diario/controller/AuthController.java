package com.diario.controller;

import com.diario.model.Usuario;
import com.diario.model.UsuarioDetails;
import com.diario.service.UsuarioService;
import com.diario.service.PasswordResetService;
import com.diario.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.buscarPorUsername(usuario.getUsername());

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();

            if (passwordEncoder.matches(usuario.getSenha(), existente.getSenha())) {
                String token = jwtUtil.generateToken(new UsuarioDetails(existente));
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "usuarioId", existente.getId()
                ));
            }
        }

        return ResponseEntity.status(401).body("Usuário ou senha inválidos");
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {
        Usuario novoUsuario = usuarioService.cadastrarUsuario(usuario);
        return ResponseEntity.ok(novoUsuario);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        passwordResetService.solicitarResetDeSenha(email);
        return ResponseEntity.ok("Link de redefinição de senha enviado para seu email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String novaSenha = body.get("novaSenha");

        try {
            passwordResetService.redefinirSenha(token, novaSenha);
            return ResponseEntity.ok("Senha redefinida com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/test-email")
    public void enviarEmailTeste() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("teste@exemplo.com");
        message.setSubject("Teste de E-mail");
        message.setText("Se tu tá vendo isso no Mailtrap, funcionou! 🚀");
        mailSender.send(message);
    }
}
