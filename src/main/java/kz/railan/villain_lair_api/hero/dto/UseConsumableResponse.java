package kz.railan.villain_lair_api.hero.dto;

public record UseConsumableResponse(
        Long consumableId,
        String itemName,
        Integer healAmount,
        Integer heroHealthAfter
) {
}
