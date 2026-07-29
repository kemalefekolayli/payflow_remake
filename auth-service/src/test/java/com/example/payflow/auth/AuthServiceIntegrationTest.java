package com.example.payflow.auth;

import com.example.payflow.auth.dto.AuthResponse;
import com.example.payflow.auth.dto.LoginRequest;
import com.example.payflow.auth.dto.RegisterRequest;
import com.example.payflow.auth.security.AuthenticatedUserPrincipal;
import com.example.payflow.auth.security.JwtService;
import com.example.payflow.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AuthServiceApplication.class)
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void registrationReturnsJwtWithImmutableIdentityClaims() {
        AuthResponse response = authService.RegisterUser(RegisterRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secret1")
                .build());

        Long userId = jwtService.extractUserId(response.getToken());
        String username = jwtService.extractClaim(response.getToken(), claims -> claims.get("username", String.class));
        String role = jwtService.extractClaim(response.getToken(), claims -> claims.get("role", String.class));

        assertThat(userId).isPositive();
        assertThat(username).isEqualTo("alice");
        assertThat(role).isEqualTo("USER");
        assertThat(jwtService.extractUsername(response.getToken())).isEqualTo("alice");

        AuthenticatedUserPrincipal principal = jwtService.parsePrincipal(response.getToken());
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.username()).isEqualTo("alice");
        assertThat(principal.authorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        AuthResponse loginResponse = authService.LoginUser(LoginRequest.builder()
                .username("alice")
                .password("secret1")
                .build());
        assertThat(jwtService.extractUserId(loginResponse.getToken())).isEqualTo(userId);
    }
}
