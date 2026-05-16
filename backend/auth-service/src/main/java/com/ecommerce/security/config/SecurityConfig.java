package com.ecommerce.security.config;

import com.ecommerce.security.filters.JwtAuthFilter;
//import com.ecommerce.security.handler.OAuth2SuccessHandler;
import com.ecommerce.security.handler.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                //for google Auth OAuth2 Must not be STATELESS for OAuth2 work
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .sessionManagement(session ->session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()        // ✅ REST endpoints
                        .requestMatchers("/login/**", "/oauth2/**").permitAll() // ✅ OAuth2
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                //OAuth2 login config
                .oauth2Login(oauth2 -> oauth2
                        // ✅ THIS is the fix — one line added
                        // Tells Spring: only redirect to Google
                        // when /oauth2/authorization/google is called
                        // NOT when /auth/login is called
                        .loginPage("/oauth2/authorization/google")
                        .successHandler(oAuth2SuccessHandler)
                )
                // ✅ Disable form login completely
                .formLogin(form -> form.disable())
                // ✅ Disable httpBasic
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}