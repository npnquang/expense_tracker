package james.expense_tracker.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        return http
                // REST API: disable CSRF since we are not using browser sessions
                .csrf(AbstractHttpConfigurer::disable)
                // Not to create/user HTTP sessions
                // (since we are not using browser sessions)
                // Each request must authenticate itself
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // public endpoints (no auth required)
                // other endpoints require authentication
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/auth/**")
                                        .permitAll()
                                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                // Keep API behavior clean (no default login page/basic prompt)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        handling ->
                                handling.authenticationEntryPoint(
                                                (request, response, authException) -> {
                                                    response.setStatus(
                                                            HttpServletResponse.SC_UNAUTHORIZED);
                                                    response.setContentType(
                                                            MediaType.APPLICATION_JSON_VALUE);
                                                    response.getWriter()
                                                            .write(
                                                                    "{\"status\":401,\"error\":\"Unauthorized\",\"detail\":\"Authentication is required or token is invalid\"}");
                                                })
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) -> {
                                                    response.setStatus(
                                                            HttpServletResponse.SC_FORBIDDEN);
                                                    response.setContentType(
                                                            MediaType.APPLICATION_JSON_VALUE);
                                                    response.getWriter()
                                                            .write(
                                                                    "{\"status\":403,\"error\":\"Forbidden\",\"detail\":\"You do not have permission to access this resource\"}");
                                                }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
