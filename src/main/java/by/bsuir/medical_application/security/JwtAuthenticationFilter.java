package by.bsuir.medical_application.security;

import by.bsuir.medical_application.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), requestPath);
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (!StringUtils.hasText(jwt)) {
                log.debug("No JWT token found in request to {}", requestPath);
            } else {
                log.debug("JWT token found for request to {}, validating...", requestPath);
                
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    log.debug("JWT token valid for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("UserDetails loaded for: {}", username);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication set in security context for user: {}", username);
                } else {
                    log.warn("JWT token validation failed for request to {}", requestPath);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for {}: {}", requestPath, ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken != null ? bearerToken.substring(0, Math.min(20, bearerToken.length())) + "..." : "null");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Extracted JWT token (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));
            return token;
        }
        
        if (StringUtils.hasText(bearerToken)) {
            log.warn("Authorization header present but does not start with 'Bearer '. Header value: {}", 
                    bearerToken.substring(0, Math.min(30, bearerToken.length())));
        }
        
        return null;
    }
}

