package kz.railan.villain_lair_api.security;

import kz.railan.villain_lair_api.battle.service.BattleService;
import kz.railan.villain_lair_api.common.config.OpenApiConfig;
import kz.railan.villain_lair_api.common.config.SecurityConfig;
import kz.railan.villain_lair_api.common.security.CustomUserDetailsService;
import kz.railan.villain_lair_api.common.security.JwtService;
import kz.railan.villain_lair_api.hero.controller.HeroController;
import kz.railan.villain_lair_api.hero.service.HeroService;
import kz.railan.villain_lair_api.villain.controller.VillainController;
import kz.railan.villain_lair_api.villain.service.VillainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HeroController.class, VillainController.class})
@Import({SecurityConfig.class, OpenApiConfig.class})
class SecurityTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean HeroService heroService;
    @MockitoBean VillainService villainService;
    @MockitoBean BattleService battleService;

    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void heroEndpoint_forbiddenForVillain() throws Exception {
        mockMvc.perform(get("/api/v1/heroes/me")
                        .with(user("villain@test.com").roles("VILLAIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void villainEndpoint_forbiddenForHero() throws Exception {
        mockMvc.perform(get("/api/v1/villains/me")
                        .with(user("hero@test.com").roles("HERO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void heroEndpoint_allowedForHero() throws Exception {
        mockMvc.perform(get("/api/v1/heroes/me")
                        .with(user("hero@test.com").roles("HERO")))
                .andExpect(status().isOk());
    }

    @Test
    void villainEndpoint_allowedForVillain() throws Exception {
        mockMvc.perform(get("/api/v1/villains/me")
                        .with(user("villain@test.com").roles("VILLAIN")))
                .andExpect(status().isOk());
    }

    @Test
    void heroEndpoint_unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/heroes/me"))
                .andExpect(status().isUnauthorized());
    }
}
