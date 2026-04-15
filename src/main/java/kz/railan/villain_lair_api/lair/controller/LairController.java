package kz.railan.villain_lair_api.lair.controller;

import java.util.List;
import kz.railan.villain_lair_api.lair.dto.LairPublicResponse;
import kz.railan.villain_lair_api.lair.service.LairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lairs")
public class LairController {
    @Autowired
    private LairService lairService;

    @GetMapping
    public List<LairPublicResponse> listLairs() {
        return lairService.listPublicLairs();
    }
}
