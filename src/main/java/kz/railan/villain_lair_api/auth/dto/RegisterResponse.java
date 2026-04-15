package kz.railan.villain_lair_api.auth.dto;

import kz.railan.villain_lair_api.user.entity.Role;

public record RegisterResponse(Long id, String username, Role role) {
}
