package kz.railan.villain_lair_api.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import kz.railan.villain_lair_api.shop.dto.PurchaseResponse;
import kz.railan.villain_lair_api.shop.dto.ShopItemResponse;
import kz.railan.villain_lair_api.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shop", description = "Browse and purchase items. The item list is filtered by the caller's role: heroes see weapons and potions, villains see defense units.")
@RestController
@RequestMapping("/api/v1/shop/items")
public class ShopController {
    @Autowired
    private ShopService shopService;

    @Operation(summary = "Browse shop", description = "Returns all active items available for the caller's role. Heroes see weapons (Basic Sword, Iron Sword, Plasma Blade) and consumables (Health Potion). Villains see defense units (Guard, Trap, Defense Turret).")
    @GetMapping
    public List<ShopItemResponse> getItems(Principal principal) {
        return shopService.getItemsForCurrentUser(principal.getName());
    }

    @Operation(summary = "Buy item", description = "Purchases the item and deducts its price from the caller's coin balance. Heroes receive weapons in their inventory or potions in their consumable inventory. Villains receive defense units added directly to their lair.")
    @PostMapping("/{itemId}/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse buyItem(Principal principal, @PathVariable Long itemId) {
        return shopService.buyItem(principal.getName(), itemId);
    }
}
