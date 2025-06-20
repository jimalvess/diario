package com.diario.service;

import com.diario.model.PasswordResetToken;
import com.diario.model.Usuario;
import com.diario.repository.PasswordResetTokenRepository;
import com.diario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void solicitarResetDeSenha(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Remove token anterior
            tokenRepository.deleteByUsuario(usuario);

            // Gera novo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(null, token, usuario, LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);

            // Pega a porta do servidor da variável de ambiente ou mete a que eu to desenvolvendo:
            String serverPort = System.getenv("SERVER_PORT");
            if (serverPort == null) {
                serverPort = "5173";
            }

            String resetLink = "http://localhost:" + serverPort + "/redefinir-senha?token=" + token;

            // Envia e-mail
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Recuperação de Senha");
            mail.setText("Clique no link para redefinir sua senha: " + resetLink);
            mailSender.send(mail);
        }
        // Se o e-mail não existir não dá erro nenhum por segurança e pra não alertar os gansos
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token inválido.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Remove token expirado
            throw new IllegalArgumentException("Token expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        tokenRepository.delete(resetToken); // Invalida o token após uso
    }
}
