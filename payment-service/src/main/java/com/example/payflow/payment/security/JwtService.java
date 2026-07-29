package com.example.payflow.payment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Long extractUserId(String token) {
        return extractUserId(parseClaims(token));
    }

    public AuthenticatedUserPrincipal parsePrincipal(String token) {
        Claims claims = parseClaims(token);

        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        if (username == null || role == null || !username.equals(claims.getSubject())) {
            throw new IllegalArgumentException("JWT does not contain the required PayFlow claims");
        }

        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new AuthenticatedUserPrincipal(
                extractUserId(claims),
                username,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("JWT is expired");
        }
        return claims;
    }

    private Long extractUserId(Claims claims) {
        Object claim = claims.get("userId");
        if (!(claim instanceof Number number)) {
            throw new IllegalArgumentException("JWT userId claim must be numeric");
        }
        long userId = number.longValue();
        if (userId <= 0) {
            throw new IllegalArgumentException("JWT userId claim must be positive");
        }
        return userId;
    }
}
