package com.shiro.elysiae.config;


import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtUtils jwtUtils;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Static frontend files
                        .requestMatchers(
                                "/", "/index.html",
                                "/css/**", "/js/**", "/images/**", "/fonts/**",
                                "/*.html", "/**/*.html", "/*.js", "/**/*.js",
                                "/*.css", "/**/*.css", "/*.ico",
                                "/login/**", "/profile/**", "/dashboard/**",
                                "/listing/**",
                                "/error/**", "/favicon.ico","/admin/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setContentType("application/json");
                            res.setCharacterEncoding("UTF-8");
                            res.setStatus(401);
                            res.getWriter().write("{\"error\":\"Unauthorized\",\"status\":401}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setContentType("application/json");
                            res.setCharacterEncoding("UTF-8");
                            res.setStatus(403);
                            res.getWriter().write("{\"error\":\"Forbidden\",\"status\":403}");
                        })
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtils),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080",
                frontendUrl
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ── JWT Filter ──────────────────────────────────────────────────────────

    static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtils jwtUtils;

        public JwtAuthenticationFilter(JwtUtils jwtUtils) {
            this.jwtUtils = jwtUtils;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = jwtUtils.extractUserId(token);

                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        if (jwtUtils.validateToken(token, userId)) {

                            boolean mustChange = jwtUtils.extractMustChangePassword(token);
                            String path = request.getServletPath();
                            if (mustChange && !path.startsWith("/api/auth/change-password")) {
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("""
                                        {"error":"PASSWORD_CHANGE_REQUIRED",
                                         "message":"You must change your password before continuing",
                                         "status":403}
                                        """);
                                return;
                            }

                            Role role = Role.valueOf(jwtUtils.extractRole(token));
                            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userId,
                                            null,
                                            List.of(authority)
                                    );
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                        } else {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Invalid token\",\"status\":401}");
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error";
                    response.getWriter().write("{\"error\":\"Token error: " + msg + "\",\"status\":401}");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}