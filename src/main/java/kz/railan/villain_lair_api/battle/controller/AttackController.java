package kz.railan.villain_lair_api.battle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import kz.railan.villain_lair_api.battle.dto.AttackRequest;
import kz.railan.villain_lair_api.battle.dto.AttackResponse;
import kz.railan.villain_lair_api.battle.dto.BattleHistoryResponse;
import kz.railan.villain_lair_api.battle.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hero", description = "Hero profile, weapon inventory, consumables, and combat actions. Requires HERO role.")
@RestController
@RequestMapping("/api/v1/heroes")
public class AttackController {
    @Autowired
    private BattleService battleService;

    @Operation(summary = "Attack a lair", description = "Launches an attack against the target lair. The hero must have a working weapon equipped. Combat outcome is determined by hero power vs lair defense with a ±3 random modifier on each side. On win: hero earns coins and lair takes damage. On loss: villain earns coins and hero takes damage.")
    @PostMapping("/attacks")
    @ResponseStatus(HttpStatus.CREATED)
    public AttackResponse attack(Principal principal, @Valid @RequestBody AttackRequest request) {
        return battleService.attack(principal.getName(), request);
    }

    @Operation(summary = "My attack history", description = "Returns all battles initiated by this hero, newest first.")
    @GetMapping("/me/attacks")
    public List<BattleHistoryResponse> history(Principal principal) {
        return battleService.getHeroBattleHistory(principal.getName());
    }
}
