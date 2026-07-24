package com.example.payflow_rewrite.Auth.Service;


import com.example.payflow_rewrite.Auth.Dto.AuthResponse;
import com.example.payflow_rewrite.Auth.Dto.LoginRequest;
import com.example.payflow_rewrite.Auth.Dto.RegisterRequest;
import com.example.payflow_rewrite.Auth.Dto.UserResponse;
import com.example.payflow_rewrite.Auth.Entity.Role;
import com.example.payflow_rewrite.Auth.Entity.UserEntity;
import com.example.payflow_rewrite.Auth.Exception.ErrorCodes;
import com.example.payflow_rewrite.Auth.Exception.GlobalException;
import com.example.payflow_rewrite.Auth.Repository.AuthRepository;
import com.example.payflow_rewrite.Auth.Security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse RegisterUser(RegisterRequest req){

        // DOES USER EXIST ? CHECK FOR USERNAME AND EMAIL
        if(authRepository.exitsByUsername(req.getUsername())) {
            throw new GlobalException(ErrorCodes.AUTH_USER_ALREADY_EXISTS);
        }
        if(authRepository.existByEmail(req.getEmail())) {
            throw new GlobalException(ErrorCodes.AUTH_USER_ALREADY_EXISTS);
        }
        UserEntity user = UserEntity.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        authRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getUsername());
        String JwtToken = jwtService.generateToken(userDetails);

        return  AuthResponse.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .token(JwtToken)
                .build();
    }

    @Transactional
    public AuthResponse LoginUser(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserEntity user = authRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new GlobalException(ErrorCodes.AUTH_USER_NOT_FOUND));

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String JwtToken = jwtService.generateToken(userDetails);

        return  AuthResponse.builder()
                .token(JwtToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();


    }

    public UserResponse getProfile(String username){
        UserEntity user = authRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException(ErrorCodes.AUTH_USER_NOT_FOUND));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
