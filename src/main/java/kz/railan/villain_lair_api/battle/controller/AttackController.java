package kz.railan.villain_lair_api.battle.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import kz.railan.villain_lair_api.battle.dto.AttackRequest;
import kz.railan.villain_lair_api.battle.dto.AttackResponse;
import kz.railan.villain_lair_api.battle.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/heroes/attacks")
public class AttackController {
    @Autowired
    private BattleService battleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttackResponse attack(Principal principal, @Valid @RequestBody AttackRequest request) {
        return battleService.attack(principal.getName(), request);
    }
}
