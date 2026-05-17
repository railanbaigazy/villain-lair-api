package kz.railan.villain_lair_api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "Register a new account and obtain a JWT token. No authentication required.")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "Register", description = "Create a new HERO or VILLAIN account. Heroes start with a Basic Sword and 100 coins. Villains start with a lair, a Starter Guard, and a Spike Trap.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Login", description = "Authenticate with email and password. Returns a Bearer JWT valid for 60 minutes.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
