package kz.railan.villain_lair_api.shop.dto;

import kz.railan.villain_lair_api.shop.entity.ShopItemCategory;

public record ShopItemResponse(
        Long id,
        String code,
        String name,
        ShopItemCategory category,
        Integer price,
        Integer power,
        Integer durability,
        String description
) {
}
