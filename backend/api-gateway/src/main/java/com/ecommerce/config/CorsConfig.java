package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;  // ✅ reactive
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

//    @Bean
//    public CorsWebFilter corsWebFilter() {
//
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:4200"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        // ✅ reactive import — not servlet
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return new CorsWebFilter(source);  // ✅ takes reactive source
//    }

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            String origin = request.getHeaders().getOrigin();
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            boolean originAllowed = origin != null && (origins.contains(origin) || origins.contains("*"));

            // ✅ Add CORS headers to every response
            headers.set("Access-Control-Allow-Origin", originAllowed ? origin : origins.get(0));
            headers.set("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            headers.set("Access-Control-Allow-Headers",
                    "Authorization, Content-Type, Accept, X-Requested-With");
            headers.set("Access-Control-Allow-Credentials", "true");
            headers.set("Access-Control-Max-Age", "3600");

            // ✅ Handle OPTIONS preflight immediately — don't forward
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }

}
