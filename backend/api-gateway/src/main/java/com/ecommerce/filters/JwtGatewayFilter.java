//package com.ecommerce.filters;
//
//import com.ecommerce.util.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//@Component
//public class JwtGatewayFilter implements GlobalFilter, Ordered {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    // ✅ should use @Value
//    @Value("${app.internal.secret}")
//    private String internalSecret;
//
//    private static final List<String> PUBLIC_URLS = List.of(
//            "/auth/login",
//            "/auth/register",
//            "/auth/signin",
//            "/auth/google/callback",   // ✅ add OAuth if needed
//            "/oauth2/"
//    );
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//        String path = exchange.getRequest().getURI().getPath();
//        System.out.println(">>> PATH Koti: " + path);
//
//        if (PUBLIC_URLS.stream().anyMatch(path::contains)) {
//            return chain.filter(exchange);
//        }
//
//        String authHeader = exchange.getRequest()
//                .getHeaders()
//                .getFirst("Authorization");
//
//        System.out.println(">>> AUTH HEADER: " + authHeader);
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            System.out.println(">>> REJECTED: No/invalid auth header");
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        String token = authHeader.substring(7);
//        boolean valid = jwtUtil.isTokenValid(token);
//        System.out.println(">>> TOKEN VALID: " + valid);
//
//        if (!valid) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        // ✅ Add secret internal header before forwarding this helps to allow only request from gateway
//        ServerWebExchange mutatedExchange = exchange.mutate()
//                .request(exchange.getRequest().mutate()
//                        .header("X-Internal-Secret", internalSecret)
//                        .build())
//                .build();
//
//        return chain.filter(mutatedExchange);
//    }
//
//    @Override
//    public int getOrder() {
//        return -1;
//    }
//}

package com.ecommerce.filters;

import com.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.internal.secret}")
    private String internalSecret;

    // ✅ Fully public — no token needed
    private static final List<String> PUBLIC_URLS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/signin",
            "/auth/google/callback",
            "/login",
            "/oauth2"
    );

    // ✅ Public GET only — books browsing
    private static final List<String> PUBLIC_GET_URLS = List.of(
            "/api/books",           // covers /api/books?page=0&size=12
            "/api/books/featured",  // ✅ ADD explicitly
            "/api/books/search",
            "/api/books/genre",
            "/api/books/bestsellers",
            "/api/books/new-arrivals",
            "/api/books/price-range",
            "/api/books/genres"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        System.out.println(">>> PATH: " + path + " METHOD: " + method);

        // ✅ Allow all public URLs
        if (PUBLIC_URLS.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        // ✅ Allow GET requests to books — guests can browse
        if (method == HttpMethod.GET &&
                PUBLIC_GET_URLS.stream().anyMatch(path::startsWith)) {  // ✅ startsWith
            return chain.filter(exchange);
        }

        // ✅ Check JWT for everything else
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(">>> REJECTED: No token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            System.out.println(">>> REJECTED: Invalid token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        System.out.println(">>> ACCEPTED");

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Internal-Secret", internalSecret)
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}