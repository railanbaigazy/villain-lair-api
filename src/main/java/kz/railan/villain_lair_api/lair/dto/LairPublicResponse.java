package kz.railan.villain_lair_api.lair.dto;

public record LairPublicResponse(
        Long id,
        String name,
        Integer level,
        Integer health,
        Integer maxHealth,
        Integer securityLevel,
        String ownerUsername,
        String status
) {
}
