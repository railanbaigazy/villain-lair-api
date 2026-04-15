package kz.railan.villain_lair_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.railan.villain_lair_api.user.entity.Role;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role,
        @Size(max = 100) String lairName
) {
}
