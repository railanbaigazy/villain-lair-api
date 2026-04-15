package kz.railan.villain_lair_api.auth.dto;

import kz.railan.villain_lair_api.user.entity.Role;

public record LoginResponse(String accessToken, String tokenType, Role role) {
}
