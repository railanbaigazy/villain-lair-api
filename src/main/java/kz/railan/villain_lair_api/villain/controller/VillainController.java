package kz.railan.villain_lair_api.villain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import kz.railan.villain_lair_api.battle.dto.BattleHistoryResponse;
import kz.railan.villain_lair_api.battle.service.BattleService;
import kz.railan.villain_lair_api.lair.dto.LairOwnerResponse;
import kz.railan.villain_lair_api.lair.dto.UpdateLairRequest;
import kz.railan.villain_lair_api.villain.dto.VillainProfileResponse;
import kz.railan.villain_lair_api.villain.service.VillainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Villain", description = "Villain profile and lair management. Requires VILLAIN role.")
@RestController
@RequestMapping("/api/v1/villains")
public class VillainController {
    @Autowired
    private VillainService villainService;
    @Autowired
    private BattleService battleService;

    @Operation(summary = "My profile", description = "Returns the villain's username, coin balance, reputation, and a summary of their lair.")
    @GetMapping("/me")
    public VillainProfileResponse me(Principal principal) {
        return villainService.getMe(principal.getName());
    }

    @Operation(summary = "My lair", description = "Returns full lair details including health, security level, status, and all defense units (guards, traps, defense weapons) with their active state.")
    @GetMapping("/me/lair")
    public LairOwnerResponse getLair(Principal principal) {
        return villainService.getOwnLair(principal.getName());
    }

    @Operation(summary = "Rename lair", description = "Updates the lair's display name.")
    @PatchMapping("/me/lair")
    public LairOwnerResponse updateLair(Principal principal, @Valid @RequestBody UpdateLairRequest request) {
        return villainService.updateLair(principal.getName(), request);
    }

    @Operation(summary = "Upgrade security", description = "Increases lair security level by 1 and increments lair level. Cost: 25 + (lairLevel × 10) coins. Maximum security level is 20.")
    @PostMapping("/me/lair/upgrade-security")
    public LairOwnerResponse upgradeSecurity(Principal principal) {
        return villainService.upgradeSecurity(principal.getName());
    }

    @Operation(summary = "Repair lair", description = "Restores 25 HP to the lair, capped at max health (100). Cost: 15 + (lairLevel × 5) coins. Also restores a BREACHED lair to ACTIVE status.")
    @PostMapping("/me/lair/repair")
    public LairOwnerResponse repairLair(Principal principal) {
        return villainService.repairLair(principal.getName());
    }

    @Operation(summary = "Incoming attack history", description = "Returns all battles where this villain's lair was the target, newest first.")
    @GetMapping("/me/lair/attacks")
    public List<BattleHistoryResponse> attacks(Principal principal) {
        return battleService.getVillainBattleHistory(principal.getName());
    }
}
