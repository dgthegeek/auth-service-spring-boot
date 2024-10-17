package com.exemple.authservice.service;

import com.exemple.authservice.dto.LoginRequest;
import com.exemple.authservice.dto.LoginResponse;
import com.exemple.authservice.dto.RegisterRequest;
import com.exemple.authservice.exception.CustomException;
import com.exemple.authservice.model.User;
import com.exemple.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Par défaut, tous les nouveaux utilisateurs ont le rôle USER

        return userRepository.save(user);
    }
}