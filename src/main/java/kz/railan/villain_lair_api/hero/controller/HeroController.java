package kz.railan.villain_lair_api.hero.controller;

import java.security.Principal;
import kz.railan.villain_lair_api.hero.dto.HeroProfileResponse;
import kz.railan.villain_lair_api.hero.service.HeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/heroes")
public class HeroController {
    @Autowired
    private HeroService heroService;

    @GetMapping("/me")
    public HeroProfileResponse me(Principal principal) {
        return heroService.getMe(principal.getName());
    }
}
