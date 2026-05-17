package kz.railan.villain_lair_api.shop.dto;

public record PurchaseResponse(
        Long shopItemId,
        String itemCode,
        String itemName,
        Integer coinsRemaining,
        String message
) {
}
