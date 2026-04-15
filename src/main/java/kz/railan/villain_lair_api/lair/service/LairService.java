package kz.railan.villain_lair_api.lair.service;

import java.util.List;
import kz.railan.villain_lair_api.lair.dto.LairPublicResponse;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LairService {
    @Autowired
    private LairRepository lairRepository;

    @Transactional(readOnly = true)
    public List<LairPublicResponse> listPublicLairs() {
        return lairRepository.findAll().stream()
                .map(lair -> new LairPublicResponse(
                        lair.getId(),
                        lair.getName(),
                        lair.getLevel(),
                        lair.getHealth(),
                        lair.getSecurityLevel()
                ))
                .toList();
    }
}
