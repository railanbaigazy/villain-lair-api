package kz.railan.villain_lair_api.hero.dto;

public record InventoryItemResponse(
        Long inventoryItemId,
        String weaponCode,
        String weaponName,
        Integer attackBonus,
        Integer currentDurability,
        Integer maxDurability,
        Boolean equipped
) {
}
