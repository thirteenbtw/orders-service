package com.thirteenbtw.orders.service;

import com.thirteenbtw.orders.domain.Role;
import com.thirteenbtw.orders.domain.User;
import com.thirteenbtw.orders.dto.Dtos.*;
import com.thirteenbtw.orders.repo.UserRepository;
import com.thirteenbtw.orders.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email уже зарегистрирован");
        }
        User u = users.save(new User(email, encoder.encode(req.password()), Role.USER));
        return new TokenResponse(jwt.issue(u.getId(), u.getRole()));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));
        return new TokenResponse(jwt.issue(u.getId(), u.getRole()));
    }
}
