package kz.railan.villain_lair_api.auth.controller;

import jakarta.validation.Valid;
import kz.railan.villain_lair_api.auth.dto.LoginRequest;
import kz.railan.villain_lair_api.auth.dto.LoginResponse;
import kz.railan.villain_lair_api.auth.dto.RegisterRequest;
import kz.railan.villain_lair_api.auth.dto.RegisterResponse;
import kz.railan.villain_lair_api.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
