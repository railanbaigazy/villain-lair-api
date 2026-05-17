package kz.railan.villain_lair_api.event.repository;

import kz.railan.villain_lair_api.event.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    Optional<UserStats> findByUserId(Long userId);
}
