// order-service → config/SecurityConfig.java
package com.ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.internal.secret}")  // ✅ reads from yml
    private String internalSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Gateway already validated JWT
                );
        return http.build();
    }


    @Bean
    public OncePerRequestFilter internalSecretFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                String secret = request.getHeader("X-Internal-Secret");
                System.out.println(">>> RECEIVED SECRET: " + secret);           // add this
                System.out.println(">>> EXPECTED SECRET: " + internalSecret);   // add this
                if (internalSecret.equals(secret)) {
                    // ✅ Request came from gateway — allow it
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    "gateway", null, List.of()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    filterChain.doFilter(request, response);
                } else {
                    // ❌ Direct access — reject
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Access only allowed through gateway");
                }
            }
        };
    }
}