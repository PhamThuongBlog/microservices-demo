package vhbtpm.k22a.api_gateway;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {

    // Phải trùng khớp với SECRET bên user-service
    private static final String SECRET = "DayLaMotChuoiBiMatRatDaiDeKyJwtToken123456";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Cho phép đi qua tự do nếu là API đăng nhập
        if (path.contains("/api/users/login")) {
            return chain.filter(exchange);
        }
        // Cho phép đi qua tự do nếu là API xem products
        if (path.contains("/api/catalog/products")) {
            return chain.filter(exchange);
        }

        // Kiểm tra Header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            // Giải mã và xác thực Token
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Đính kèm username vào header để chuyển tiếp xuống microservice bên dưới
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Name", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}