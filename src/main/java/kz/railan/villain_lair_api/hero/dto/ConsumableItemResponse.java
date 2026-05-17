package kz.railan.villain_lair_api.hero.dto;

public record ConsumableItemResponse(
        Long id,
        String itemCode,
        String itemName,
        Integer healAmount,
        Integer quantity
) {
}
