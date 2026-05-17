package kz.railan.villain_lair_api.economy.repository;

import kz.railan.villain_lair_api.economy.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
