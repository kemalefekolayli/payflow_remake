package com.example.payflow_rewrite.Auth.Controller;


import com.example.payflow_rewrite.Auth.Dto.AuthResponse;
import com.example.payflow_rewrite.Auth.Dto.LoginRequest;
import com.example.payflow_rewrite.Auth.Dto.RegisterRequest;
import com.example.payflow_rewrite.Auth.Dto.UserResponse;
import com.example.payflow_rewrite.Auth.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
            AuthResponse authResponse = authService.RegisterUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.LoginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        UserResponse response = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
