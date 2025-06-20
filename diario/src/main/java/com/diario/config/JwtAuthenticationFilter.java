package com.diario.config;

import com.diario.model.UsuarioDetails;
import com.diario.service.CustomUserDetailsService;
import com.diario.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Este método é sobrescrito para especificar quais requisições NÃO devem ser filtradas por este filtro JWT.
     * Permite que requisições para caminhos de arquivo públicos e rotas de autenticação/redefinição de senha
     * passem sem verificação de token.
     *
     * @param request A requisição HTTP.
     * @return true se a requisição não deve ser filtrada por este filtro, false caso contrário.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Exclui rotas públicas da verificação de token JWT:
        return path.startsWith("/api/entradas/arquivo/") ||
                path.startsWith("/auth/") ||
                path.startsWith("/redefinir-senha/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // Verifica se o cabeçalho de autorização está presente e começa com "Bearer "
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim(); // E remove o "Bearer "

                if (token.isEmpty()) {
                    logger.warn("Token JWT vazio no header para a requisição: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Pega o nome de usuário do token
                username = jwtUtil.extractUsername(token);
            } else {
                logger.warn("Token não encontrado ou inválido no header para a requisição: {}", request.getRequestURI());
                filterChain.doFilter(request, response); // Passa pro próximo filtro
                return; // Deita o cabelo pra não continuar processando sem um token
            }

            // Se o nome de usuário foi extraído e não há autenticação existente no contexto de segurança:
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Autenticando usuário: " + username + " para requisição: " + request.getRequestURI());
                UsuarioDetails userDetails = (UsuarioDetails) userDetailsService.loadUserByUsername(username);

                // Verifica se o token é válido pro usuário
                if (jwtUtil.isTokenValid(token, userDetails)) {
                    logger.info("Token válido, autenticando o usuário no contexto para: " + username);

                    // Cria o token de autenticação
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // Credenciais são nulas depois da autenticação
                                    userDetails.getAuthorities()
                            );
                    // Define os detalhes da autenticação pro contexto de segurança
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Define a autenticação no contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.warn("Token inválido para o usuário: " + username + " na requisição: " + request.getRequestURI());
                }
            }

            // Passa pro próximo filtro na cadeia
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // Captura exceções específicas do JWT (malformado, expirado, etc.)
            logger.error("Erro JWT na requisição {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext(); // Limpa o contexto de segurança

            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                response.setContentType("application/json");
                response.getWriter().write("{\"erro\": \"Token JWT inválido ou expirado\"}");
            }
        } catch (Exception e) {
            // Captura outras exceções inesperadas durante o processo do filtro:
            logger.error("Erro inesperado no filtro JWT para requisição {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();

            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error :-(
                response.setContentType("application/json");
                response.getWriter().write("{\"erro\": \"Erro interno no servidor de autenticação\"}");
            }
        }
    }
}
