package io.github.fernando_mariani.TechFixOrg.config;

import io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes;
import io.github.fernando_mariani.TechFixOrg.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            service.verificarIp(request.getRemoteAddr());
        } catch (LimiteRequisicoes exception) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(exception.getRetryAfter()));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":429,\"mensagem\":\"Limite de requisições excedido. Tente novamente mais tarde.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
