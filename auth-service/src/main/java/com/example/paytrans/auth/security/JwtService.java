package com.example.paytrans.auth.security;

import com.example.paytrans.auth.entity.UserEntity;
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
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long jwtExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long jwtExpiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(UserEntity user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractUserId(extractAllClaims(token));
    }

    public AuthenticatedUserPrincipal parsePrincipal(String token) {
        Claims claims = extractAllClaims(token);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        if (username == null || role == null || !username.equals(claims.getSubject())) {
            throw new IllegalArgumentException("JWT does not contain the required PayTrans claims");
        }

        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new AuthenticatedUserPrincipal(
                extractUserId(claims),
                username,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
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
