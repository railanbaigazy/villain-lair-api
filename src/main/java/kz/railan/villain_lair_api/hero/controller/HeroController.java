package kz.railan.villain_lair_api.hero.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import kz.railan.villain_lair_api.hero.dto.ConsumableItemResponse;
import kz.railan.villain_lair_api.hero.dto.HeroProfileResponse;
import kz.railan.villain_lair_api.hero.dto.InventoryItemResponse;
import kz.railan.villain_lair_api.hero.dto.UseConsumableResponse;
import kz.railan.villain_lair_api.hero.service.HeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hero", description = "Hero profile, weapon inventory, consumables, and combat actions. Requires HERO role.")
@RestController
@RequestMapping("/api/v1/heroes")
public class HeroController {
    @Autowired
    private HeroService heroService;

    @Operation(summary = "My profile", description = "Returns the hero's username, coin balance, base attack, current health, and currently equipped weapon.")
    @GetMapping("/me")
    public HeroProfileResponse me(Principal principal) {
        return heroService.getMe(principal.getName());
    }

    @Operation(summary = "My weapon inventory", description = "Lists all weapons owned by the hero with their current durability and equipped status.")
    @GetMapping("/me/inventory")
    public List<InventoryItemResponse> inventory(Principal principal) {
        return heroService.getInventory(principal.getName());
    }

    @Operation(summary = "Equip a weapon", description = "Equips the specified inventory item. Automatically unequips the current weapon. Broken weapons (durability 0) cannot be equipped.")
    @PostMapping("/me/inventory/{inventoryItemId}/equip")
    public InventoryItemResponse equip(Principal principal, @PathVariable Long inventoryItemId) {
        return heroService.equipWeapon(principal.getName(), inventoryItemId);
    }

    @Operation(summary = "My consumables", description = "Lists all consumable items (potions) in the hero's inventory with their quantity and heal amount.")
    @GetMapping("/me/inventory/consumables")
    public List<ConsumableItemResponse> consumables(Principal principal) {
        return heroService.getConsumables(principal.getName());
    }

    @Operation(summary = "Use a consumable", description = "Consumes one use of the specified item to restore HP (Health Potion: +30 HP, max 100). The item is removed from inventory when quantity reaches 0. Cannot be used at full health.")
    @PostMapping("/me/inventory/consumables/{consumableItemId}/use")
    public UseConsumableResponse useConsumable(Principal principal, @PathVariable Long consumableItemId) {
        return heroService.useConsumable(principal.getName(), consumableItemId);
    }
}
