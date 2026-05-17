package kz.railan.villain_lair_api.battle.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AttackRequest(@NotNull @Positive Long targetLairId) {
}
