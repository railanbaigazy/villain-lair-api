package kz.railan.villain_lair_api.battle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import kz.railan.villain_lair_api.battle.dto.BattleDetailResponse;
import kz.railan.villain_lair_api.battle.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Battles", description = "Retrieve battle details. Only participants (the attacking hero and the defending villain) can view a battle.")
@RestController
@RequestMapping("/api/v1/battles")
public class BattleController {
    @Autowired
    private BattleService battleService;

    @Operation(summary = "Get battle detail", description = "Returns full detail for a single battle including both sides' power scores, damage dealt, hero damage received, and coin rewards. Returns 403 if the caller was not a participant.")
    @GetMapping("/{battleId}")
    public BattleDetailResponse getBattle(Principal principal, @PathVariable Long battleId) {
        return battleService.getBattleForParticipant(principal.getName(), battleId);
    }
}
