package kz.railan.villain_lair_api.lair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import kz.railan.villain_lair_api.lair.dto.LairPublicDetailResponse;
import kz.railan.villain_lair_api.lair.dto.LairPublicResponse;
import kz.railan.villain_lair_api.lair.service.LairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lairs", description = "Public lair scouting. Available to all authenticated users. Heroes use this to find targets before attacking.")
@RestController
@RequestMapping("/api/v1/lairs")
public class LairController {
    @Autowired
    private LairService lairService;

    @Operation(summary = "List all lairs", description = "Returns a summary of all lairs: owner, level, security level, and status. BREACHED lairs are included but cannot be attacked.")
    @GetMapping
    public List<LairPublicResponse> listLairs() {
        return lairService.listPublicLairs();
    }

    @Operation(summary = "Get lair detail", description = "Returns public detail for a single lair including all defense units with their type and power. Use this to estimate defense strength before committing to an attack.")
    @GetMapping("/{lairId}")
    public LairPublicDetailResponse getLair(Principal principal, @PathVariable Long lairId) {
        return lairService.getLairDetail(principal.getName(), lairId);
    }
}
