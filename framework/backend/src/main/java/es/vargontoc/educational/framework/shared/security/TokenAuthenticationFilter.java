package es.vargontoc.educational.framework.shared.security;

import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final FamilySessionUseCase familySessionUseCase;

    public TokenAuthenticationFilter(FamilySessionUseCase familySessionUseCase) {
        this.familySessionUseCase = familySessionUseCase;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var token = extractToken(request);
            if (token != null) {
                var session = familySessionUseCase.getByToken(token);
                setAuthentication(session);
            }
        } catch (SessionException exception) {
            SecurityContextHolder.clearContext();
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private static String extractToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length());
    }

    private static void setAuthentication(FamilySession session) {
        var authentication = new UsernamePasswordAuthenticationToken(
            session.getFamilyId(),
            null,
            List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
