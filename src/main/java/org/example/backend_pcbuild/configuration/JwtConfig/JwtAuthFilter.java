package org.example.backend_pcbuild.configuration.JwtConfig;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;

@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private UserAuthProvider userAuthProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header != null) {
            String[] elements = header.split(" ");
            if(elements.length == 2 && "Bearer".equals(elements[0])) {
                try{
                    SecurityContextHolder.getContext().setAuthentication(
                            userAuthProvider.validateToken(elements[1])
                    );
                } catch (ResponseStatusException e) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(e.getStatusCode().value());
                    response.getWriter().write(Objects.requireNonNull(e.getReason()));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
