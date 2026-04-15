package kz.railan.villain_lair_api.battle.dto;

import jakarta.validation.constraints.NotNull;

public record AttackRequest(@NotNull Long targetLairId) {
}
