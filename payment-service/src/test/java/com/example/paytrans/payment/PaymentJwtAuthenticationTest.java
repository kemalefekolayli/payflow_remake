package com.example.paytrans.payment;

import com.example.paytrans.payment.security.JwtAuthenticationFilter;
import com.example.paytrans.payment.security.JwtService;
import com.example.paytrans.payment.security.AuthenticatedUserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentJwtAuthenticationTest {
    private static final String SECRET =
            "cGF5dHJhbnMtdGVzdC1zZWNyZXQta2V5LW11c3QtYmUtbG9uZy1lbm91Z2gtMzItYnl0ZXM=";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesLocallyFromJwtClaims() throws Exception {
        String token = Jwts.builder()
                .subject("alice")
                .claim("userId", 42L)
                .claim("username", "alice")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), Jwts.SIG.HS256)
                .compact();

        JwtService jwtService = new JwtService(SECRET);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);

        AuthenticatedUserPrincipal parsedPrincipal = jwtService.parsePrincipal(token);
        assertThat(parsedPrincipal.userId()).isEqualTo(42L);
        assertThat(parsedPrincipal.username()).isEqualTo("alice");
        assertThat(parsedPrincipal.authorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_USER");

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Object authenticatedPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(authenticatedPrincipal)
                .isEqualTo(parsedPrincipal);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_USER");
    }
}
