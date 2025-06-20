package com.diario.config;

import com.diario.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST stateless
                .cors(cors -> {}) // Habilita CORS configurado no WebConfig (lambda vazia, pois a configuração é no WebConfig)
                .authorizeRequests(auth -> auth // Inicia a configuração de autorização com lambda
                        .antMatchers("/auth/**").permitAll() // Permite acesso público a endpoints de autenticação
                        .antMatchers("/auth/test-email").permitAll() // Permite acesso público a endpoint de teste de email
                        .antMatchers("/redefinir-senha/**").permitAll() // Permite acesso público para redefinição de senha
                        .antMatchers("/api/entradas/arquivo/**").permitAll() // Permite acesso público aos arquivos de mídia
                        .anyRequest().authenticated() // Todas as outras requisições precisam de autenticação
                ) // Fecha a configuração de autorização, retorna HttpSecurity
                .sessionManagement(session -> session // Inicia a configuração de gerenciamento de sessão com lambda
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Define política de sessão como stateless (sem sessão HTTP)
                ) // Fecha a configuração de sessão, retorna HttpSecurity
                .authenticationProvider(authenticationProvider()) // Configura o provedor de autenticação
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Configuração do Content-Security-Policy para permitir iframes de origens específicas (lance do preview do PDF)
                .headers(headers -> headers // Inicia a configuração de cabeçalhos com lambda
                        .contentSecurityPolicy(csp -> csp // Configura Content Security Policy
                                .policyDirectives("frame-ancestors 'self' http://localhost:5173") // Permite 'self' (localhost:8080) e a origem do frontend em outra porta
                        )
                ) // Fecha a configuração de cabeçalhos, retorna HttpSecurity
                .build(); // Constrói a cadeia de filtros de segurança
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
