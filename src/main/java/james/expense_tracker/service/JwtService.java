package james.expense_tracker.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final Long accessExpMs;
    private final Long refreshExpMs;

    public JwtService(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.access-expiration-ms}") Long accessExpMs,
            @Value("${jwt.refresh-expiration-ms}") Long refreshExpMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyString));
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, "access", accessExpMs, null);
    }

    public String generateRefreshToken(Long userId, String username) {
        String jti = UUID.randomUUID().toString();
        return buildToken(userId, username, null, "refresh", refreshExpMs, jti);
    }

    private String buildToken(
            Long userId, String username, String role, String type, Long expMs, String jti) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        JwtBuilder builder =
                Jwts.builder()
                        .subject(username)
                        .claim("uid", userId)
                        .claim("type", type)
                        .issuedAt(now);

        if (role != null) {
            builder.claim("role", role);
        }

        if (jti != null) {
            builder.id(jti);
        }

        return builder.expiration(exp).signWith(secretKey).compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseToken(token).get("type", String.class));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).get("type", String.class));
    }

    public Long getAccessExpMs() {
        return accessExpMs;
    }

    public Long getRefreshExpMs() {
        return refreshExpMs;
    }
}
