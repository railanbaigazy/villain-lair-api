package kz.railan.villain_lair_api.event.repository;

import kz.railan.villain_lair_api.event.entity.GameEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameEventLogRepository extends JpaRepository<GameEventLog, Long> {

    List<GameEventLog> findTop100ByOrderByConsumedAtDesc();

    List<GameEventLog> findTop100ByEventTypeOrderByConsumedAtDesc(String eventType);
}
